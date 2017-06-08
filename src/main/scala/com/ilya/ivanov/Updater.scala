package com.ilya.ivanov

import javafx.scene.control.TreeItem
import scala.collection.JavaConverters._

/**
  * Created by ilya on 6/2/17.
  */
object Updater {
  def flat[T](s: java.util.Collection[java.util.Collection[T]]): java.util.Collection[T] = {
    s.asScala.flatMap(_.asScala).asJavaCollection
  }

  def findItem[T](root: TreeItem[T], item: T): TreeItem[T] = {
    if (root.getValue.equals(item)) return root
    else root.getChildren.asScala.foldLeft[TreeItem[T]](null)((a, b) => if (a != null) a else findItem(b, item))
  }

  def findItems[T](root: TreeItem[T], items: Array[T]): java.util.Set[TreeItem[T]] = {
    val targetItems = scala.collection.mutable.Set(items: _*)
    val foundedItems = scala.collection.mutable.Set.empty[TreeItem[T]]
    def traversal(i: TreeItem[T]): Unit = {
      if (targetItems.isEmpty) return
      else if (targetItems.contains(i.getValue)) {
        foundedItems.add(i)
        targetItems.remove(i.getValue)
      } else i.getChildren.asScala.foreach(c => traversal(c))
    }
    traversal(root)
    foundedItems.asJava
  }
}
