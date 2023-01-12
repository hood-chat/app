import { flow, Instance, SnapshotOut, types } from "mobx-state-tree"
import { createContactStoreDefaultModel } from "./ContactStore"
import { createChatListDefaultModel } from "./ChatStore"
import { createIdentityDefaultModel } from "./Identity"
import { createPermissionsDefaultModel } from "./Permissions"

/**
 * A RootStore model.
 */
export const RootStoreModel = types.model("RootStore").props({
  contactStore: createContactStoreDefaultModel(),
  identityStore: createIdentityDefaultModel(),
  chatStore: createChatListDefaultModel(),
  permissionStore: createPermissionsDefaultModel()
})

/**
 * The RootStore instance.
 */
export interface RootStore extends Instance<typeof RootStoreModel> { }
/**
 * The data of a RootStore.
 */
export interface RootStoreSnapshot extends SnapshotOut<typeof RootStoreModel> { }
