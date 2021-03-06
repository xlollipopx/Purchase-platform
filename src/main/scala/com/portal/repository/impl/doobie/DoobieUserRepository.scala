package com.portal.repository.impl.doobie

import cats.Functor
import cats.effect.Bracket
import com.portal.domain.auth.{EncryptedPassword, PhoneNumber, UserId, UserName}
import com.portal.http.auth.users.{User, UserWithPassword}
import com.portal.repository.UserRepository
import doobie.{ConnectionIO, Fragment, Transactor}
import doobie.postgres.implicits._
import doobie.implicits._
import meta.implicits._
import cats.implicits._

import java.util.UUID

class DoobieUserRepository[F[_]: Functor: Bracket[*[_], Throwable]](
  tx: Transactor[F]
) extends UserRepository[F] {
  private val selectUser: Fragment = fr"SELECT * FROM users"
  private val createUser: Fragment = fr"INSERT INTO users(" ++
    fr"uuid, name, mail, role, password)"
  private val createCourier: Fragment = fr"INSERT INTO courier_info(" ++
    fr"uuid, phone_number)"

  override def all(): F[List[UserWithPassword]] = selectUser.query[UserWithPassword].to[List].transact(tx)

  override def createUser(user: User, password: EncryptedPassword): F[Int] =
    create(user: User, password: EncryptedPassword)
      .transact(tx)

  override def findByName(name: UserName): F[Option[UserWithPassword]] = {
    (selectUser ++ fr"WHERE name = ${name}").query[UserWithPassword].option.transact(tx)
  }

  override def createCourier(user: User, phoneNumber: PhoneNumber, password: EncryptedPassword): F[Int] = {
    val res = for {
      _ <- create(user, password)
      a <- (createCourier ++ fr"VALUES(${user.id}, $phoneNumber)").update.run
    } yield a
    res.transact(tx)
  }

  private def create(user: User, password: EncryptedPassword): ConnectionIO[Int] = {
    (createUser ++ fr"VALUES(${user.id}, ${user.name}, ${user.mail}, ${user.role}, ${password})").update.run
  }

}
