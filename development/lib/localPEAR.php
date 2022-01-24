<?php

require_once('for_php7.php');

/**
 * PEARのインクルード、及びPEARを継承する共通で使えるサブクラスを定義する。
 */
  
require_once("PEAR.php");
require_once("DB.php");
require_once("HTML/IT.php");
require_once("pear/Application.php");


class APP_Session extends Cache_Application {
    var $gc_time        = 60;
    var $gc_probability = 5;
    var $gc_maxlifetime = 86400;

    var $container = 'file';
    var $container_options = array('cache_dir'       => '/tmp/gaku/',
                                   'filename_prefix' => 'cache_');

    function APP_Session($id, $group = 'application_cache')
    {
        $this->Cache_Application($this->container, $this->container_options, $id, $group);
    }

    function _APP_Session()
    {
        $this->_Cache_Application();

        //ガーベッジコレクションがうまく機能しないので保留
//        $this->garbageCollection();
    }
}
?>
