package com.ilya.ivanov

import javafx.scene.control.{TreeItem, TreeTableView}

import com.ilya.ivanov.data.model.file.FileEntity

/**
  * Created by ilya on 6/2/17.
  */
object Updater {
  def update[T](t: TreeTableView[T]): Unit = {
    def storeExpand(item: TreeItem[T]): List[TreeItem[T]] = {
      if (item.isLeaf) Nil
      else {
        val list = Nil
        item.getChildren.forEach(i => list :: storeExpand(i))
        if (item.isExpanded) item :: list
        else list
      }
    }
    def restoreExpand(item: TreeItem[T], l: List[TreeItem[T]]): Unit = {
      if (item.isLeaf) Nil
      else {
        if (l.contains(item)) {
          item.setExpanded(true)

        }
        item.getChildren.forEach(i => storeExpand(i))
      }
    }
    val stored = storeExpand(t.getRoot)
    t.refresh()
    restoreExpand(t.getRoot, stored)
  }
}
