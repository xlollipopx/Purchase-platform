package com.portal.repository

import cats.effect.Sync
import com.portal.repository.impl.doobie.DoobieSubscriptionRepository
import doobie.Transactor

import java.util.UUID

trait SubscriptionRepository[F[_]] {
  def createSupplierSubscription(userId: UUID, supplierId: UUID): F[Int]
  def deleteSupplierSubscription(userId: UUID, supplierId: UUID): F[Int]
  def createCategorySubscription(userId: UUID, categoryId: UUID): F[Int]
  def deleteCategorySubscription(userId: UUID, categoryId: UUID): F[Int]

}

object SubscriptionRepository {
  def of[F[_]: Sync](tx: Transactor[F]): DoobieSubscriptionRepository[F] =
    new DoobieSubscriptionRepository[F](tx)
}
