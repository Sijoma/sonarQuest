import {Artefact, AvatarClass, AvatarRace, Level} from './Developer';
import {Role} from './Role';

export interface User {
  id: number,
  username: string,
  role: Role,
  picture?: string,
  aboutMe?: string,
  avatarClass?: AvatarClass,
  avatarRace?: AvatarRace,
  gold?: number,
  xp?: number,
  level?: Level,
  artefacts?: Artefact[],
}
