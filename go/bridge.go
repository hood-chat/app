package bridge

import (
	"context"
	"net"
	"strings"

	"github.com/hood-chat/core"
	"github.com/hood-chat/core/entity"
	logging "github.com/ipfs/go-log/v2"
	ma "github.com/multiformats/go-multiaddr"
	madns "github.com/multiformats/go-multiaddr-dns"
	manet "github.com/multiformats/go-multiaddr/net"
)

var log = logging.Logger("bridge")

type Bridge struct {
	core core.Messenger
}

func NewBridge(repoPath string, conf *HostConfig) (*Bridge, error) {
	net.DefaultResolver = &DefaultResolver

	rslvOpt := madns.WithDefaultResolver(&DefaultResolver)
	maRslv, err := madns.NewResolver(rslvOpt)
	if err != nil {
		panic(err)
	}
	madns.DefaultResolver = maRslv

	hb := NewMobileHost(conf)
	if hb == nil {
		panic("new hb failed")
	}

	m := core.MessengerBuilder(repoPath, Option(), hb)
	f := &Bridge{core: m}

	return f, nil
}

func (b *Bridge) IsLogin() bool {
	return b.core.IsLogin()
}

func (b *Bridge) GetInterfaces() (string, error) {
	adders, err := manet.InterfaceMultiaddrs()
	if err != nil {
		panic("manet not work")
	}
	addr, err := ma.NewMultiaddr("/dnsaddr/bootstrap.libp2p.io/p2p/QmNnooDu7bfjPFoTZYxMNLWUQJyrVwtbZg5gBMjTezGAJN")
	if err != nil {
		panic("cant parse ma adder")
	}
	_, err = madns.Resolve(context.Background(), addr)
	if err != nil {
		log.Debugf("can not resolve host: %s", err.Error())
		// panic("madns not work")
	}
	sAdders := make([]string, 0)
	for _, val := range adders {
		sAdders = append(sAdders, val.String())
	}
	return strings.Join(sAdders, " , "), err

}

func (b *Bridge) GetIdentity() (string, error) {
	id, err := b.core.GetIdentity()
	if err != nil {
		return "", err
	}
	return Marshal(Identity{
		ID:   id.ID,
		Name: id.Name,
	})
}

func (b *Bridge) NewIdentity(name string) (string, error) {
	id, err := b.core.SignUp(name)
	if err != nil {
		return "", err
	}
	return Marshal(Identity{
		ID:   id.ID,
		Name: id.Name,
	})
}

func (b *Bridge) GetChat(id string) (string, error) {
	msgr := b.core
	ch, err := msgr.GetChat(id)
	if err != nil {
		return "", err
	}
	mids := []string{}
	for _, m := range ch.Members {
		mids = append(mids, m.ID)
	}
	return Marshal(Chat{
		ID:      ch.ID,
		Name:    ch.Name,
		Members: mids,
	})
}

func (b *Bridge) GetChats() (string, error) {
	msgr := b.core
	ch, err := msgr.GetChats()
	if err != nil {
		return "", err
	}
	chs := make([]Chat, 0)

	for _, c := range ch {
		mids := []string{}
		for _, m := range c.Members {
			mids = append(mids, m.ID)
		}
		chs = append(chs, Chat{
			ID:      c.ID,
			Name:    c.Name,
			Members: mids,
		})
	}

	return Marshal(chs)
}

func (b *Bridge) GetMessages(chatID string) (string, error) {
	msgr := b.core
	res := make([]Message, 0)

	msgs, err := msgr.GetMessages(chatID)
	if err != nil {
		return "", err
	}

	for _, msg := range msgs {
		res = append(res, Message{
			ID:        msg.ID,
			Text:      msg.Text,
			CreatedAt: msg.CreatedAt.Unix(),
			ContactID: msg.Author.ID,
			Sent:      msg.Status != entity.Pending,
			Received:  msg.Status == entity.Seen,
			Pending:   msg.Status == entity.Pending,
		})
	}
	return Marshal(res)
}

func (b *Bridge) SendMessage(chatId string, text string) (string, error) {
	msgr := b.core
	enp, err := msgr.NewMessage(chatId, text)
	if err != nil {
		return "", err
	}
	return Marshal(Message{
		ID:        enp.Msg.ID,
		Text:      enp.Msg.Text,
		CreatedAt: enp.Msg.CreatedAt.Unix(),
		ContactID: enp.Msg.Author.ID,
		Sent:      enp.Msg.Status != entity.Pending,
		Received:  enp.Msg.Status == entity.Seen,
		Pending:   enp.Msg.Status == entity.Pending,
	})
}

func (b *Bridge) GetContacts() (string, error) {
	msgr := b.core
	cs, err := msgr.GetContacts()
	if err != nil {
		return "", err
	}
	res := make([]Contact, 0)
	for _, c := range cs {
		res = append(res, Contact(c))
	}
	return Marshal(res)
}

func (b *Bridge) GetContact(id string) (string, error) {
	msgr := b.core
	c, err := msgr.GetContact(id)
	if err != nil {
		return "", err
	}
	return Marshal(Contact(c))
}

func (b *Bridge) AddContact(id string, name string) error {
	msgr := b.core
	err := msgr.AddContact(entity.Contact{
		ID:   id,
		Name: name,
	})
	return err
}

func (b Bridge) NewPMChat(contactID string) (string, error) {
	msgr := b.core
	pm, err := msgr.CreatePMChat(contactID)
	if err != nil {
		return "", err
	}

	return Marshal(toChat(pm))
}

func (b Bridge) GetPMChat(contactID string) (string, error) {
	msgr := b.core
	pm, err := msgr.GetPMChat(contactID)
	if err != nil {
		return "", err
	}
	return Marshal(toChat(pm))
}

func toChat(ch entity.ChatInfo) Chat {
	mids := []string{}
	for _, m := range ch.Members {
		mids = append(mids, m.ID)
	}
	return Chat{
		ID:      ch.ID,
		Name:    ch.Name,
		Members: mids,
	}
}
