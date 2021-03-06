package controllers

import javax.inject._
import play.api.mvc._
import models.ProductRepository
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.libs.json.Json
import play.api.data.Forms._

import scala.concurrent.{ExecutionContext, Future}

/**
  */
@Singleton
class ProductsController @Inject()(productRepository: ProductRepository,cc: ControllerComponents) (implicit ec: ExecutionContext )extends AbstractController(cc) {

  val productForm: Form[CreateProductForm] = Form {
    mapping(
      "name" -> nonEmptyText,
      "description"-> nonEmptyText,
      "price" -> number,
      "image" -> optional(nonEmptyText),
      "unavailable"-> boolean,
      "categoryId" -> number
    )(CreateProductForm.apply)(CreateProductForm.unapply)
  }

  def getAll = Action.async(
    implicit request => (
      productRepository.list().map(
        product => Ok(Json.toJson(product))
      )
      )
  )

  def getById(id: Int) = Action.async { implicit request =>
    val options = for {
      maybeProduct <- productRepository.findById(id)
    } yield (maybeProduct)

    options.map { case (opt) =>
      opt match {
        case Some(product) => Ok(Json.toJson(product))
        case None => NotFound
      }
    }
  }

  def getByCategoryId(id: Int) = Action.async(
    implicit request => (
      productRepository.findByCategoryId(id).map(
        product => Ok(Json.toJson(product))
      )
      )
  )

  def create = Action.async { implicit request =>
    productForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(BadRequest("Failed to create product."))
      },
      product => {
        productRepository.create(
          product.name,
          product.description,
          product.price,
          product.image,
          product.unavailable,
          product.categoryId
        ).map { product =>
          Created(Json.toJson((product)))
        }
      }
    )
  }

  def update(id: Int) =
    Action.async(parse.json){
      implicit request =>
        productForm.bindFromRequest.fold(
          _ => {
            Future.successful(BadRequest("Failed to update product."))
          },
          product => {
            productRepository.update(models.Product(
              id,
              product.name,
              product.description,
              product.price,
              product.image,
              product.unavailable,
              product.categoryId,
            )).map({ _ =>
              Ok
            })
          }
        )
    }

  def delete(id: Int) = Action.async(
    productRepository.delete(id).map(_ => Ok(""))
  )

}

case class CreateProductForm(name: String, description: String, price: Int, image: Option[String], unavailable: Boolean, categoryId: Int)
