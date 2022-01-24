<?php

require_once('for_php7.php');

// ----------------------------------------------------
// リロード・Submit ボタンのダブルクリック対策用クラス
//                                           2001/5/30
//                                             ver 1.0
//                          株式会社 IT Boost 松嶋祥文
// ----------------------------------------------------
//                                 mats@itboost.co.jp
//                           http://www.itboost.co.jp/
// ----------------------------------------------------

class Reload {
  var $cDir;                    // チェック用ファイル生成場所
  var $cLifetime;               // チェック用ファイル最低存続期間
  var $cGCProbability;          // GC 確率(小さいほど低確率）
  var $cRegisted;               // 生成したチェック用ファイル名（フルパス）
  var $cParamName;              // ページ間で受渡すための変数名を設定
  var $cPageID;                 // PageID

  // コンストラクタ
  function Reload() {
    mt_srand(((double)microtime())*123432);

    $this->cDir = "/tmp/.reload";
    $this->cLifetime = 600;
    $this->cGCProbability = 3;
    $this->cParamName = "_r_e_l_o_a_d_";
  }

  // リロードされたかどうかのチェック
  function isReload() {
    // 可変変数です。
    global ${$this->cParamName};
    $paramname = ${$this->cParamName};

    // 確率にしたがって GC する。
    if( mt_rand(0, 100) < $this->cGCProbability ) {
      $this->gc();
    }

    // 変数名がセットされていないときは、リロードされていないと判断。
    if( $paramname == "" ) { return false; }      

    $file = $this->cDir . "/" . escapeshellcmd($paramname);

    // 一応、file_exists のキャッシュをクリアしておきます。多分不要。
    clearstatcache();

    if( file_exists($file) ) {
      // リロードされた場合。
      return true;
    } else {
      // 正規の処理の場合は、チェック用ファイルを生成
      touch($this->cDir ."/$paramname");
      $this->cRegisted = $this->cDir . "/$paramname";
    }
    return false;
  }

  // HIDDEN タグの埋め込み
  function embed(&$objForm) {
    $this->getPageID();
    $uniqid = $this->cPageID;
    $GLOBALS[$this->cParamName] = $uniqid;
    $objForm->ae( array( "type"  => "hidden",
                         "name"  => $this->cParamName,
                         "value" => $uniqid ));
    return true;
  }

  // URL パラメータの生成
  function urlparam() {
    $this->getPageID();
    $uniqid = $this->cPageID;
    return urlencode($this->cParamName) . "=" . urlencode("$uniqid");
  }

  //
  function fileDelete() {
    $param = $this->cParamName;
    global $$param;

    $file = $this->cDir . "/". $$param;
    @unlink($file);
  }

  // 古いチェック用ファイルを削除する
  function gc() {
    $threshold = time() - $this->cLifetime;
    $dir = opendir($this->cDir); 
    while ( $file = readdir($dir) ) { 
      if ($file != "." && $file != "..") { 
	$file = $this->cDir . "/$file";
	if( $threshold > filemtime($file) ) {
	  // チェック用ファイルが古かったら削除。
	  @unlink($file);
	}
      }
    }
    closedir($dir); 
  }

  function getPageID() {
    $uniqid = md5(uniqid(microtime()));
    $this->cPageID = $uniqid;
  }

  // チェック用ファイルの置き場所設定
  function setDir($aDir) {
    if( !file_exists($aDir) ) {
      $this->er("[setDir]: Invalid arg. Directory not exists.");
    }
    // 最後の / は取り除く。
    $this->cDir = preg_replace("/\/$/", "", $aDir);
  }

  // チェック用ファイルの最低存続期間設定
  function setLifetime($aLifetime) {
    if( !is_integer($aLifetime) ) {
      $this->er("[setLifetime]: Invalid arg.");
    }
    $this->cLifetime = $aLifetime;
  }

  // GC 確率の設定
  function setGCProbablity($aGCProbablity) {
    if( !preg_match("/^[0-9]+/", $aGCProbablity) ) {
      $this->er("[setGCProbablity]: Invalid arg.");
    }
    // 0 はダメ。
    if( $aGCProbablity == 0 ) {
      $this->er("[setGCProbablity]: Can't set 0.");
    }
    $this->cGCProbability = $aGCProbablity;
  }

  // チェック用ファイル名をページ間で受渡すための変数名を設定
  function setParamName($aParamName) {
    // 変数名には a-z A-Z _ しか使えないことにします。
    if( preg_match("/[^a-zA-Z_]/", $aParamName) ) {
      $this->er("[setParamName]: Invalid arg.");
    }
    $this->cParamName = $aParamName;
  }
  
  // パラメータをすべて設定
  function setParams($aDir, $aLifetime, $aGCProbablity, $aParamName) {
    $this->setDir($aDir);
    $this->setLifetime($aLifetime);
    $this->setGCProbablity($aGCProbablity);
    $this->setParamName($aParamName);
  }

  // isReload() で生成したチェック用ファイルの名前を取得
  function getRegisted() {
    return $this->cRegisted;
  }

  // エラー出力用関数。die します。
  function er($aStr) {
    die("<hr>\n"."<b>Reload Class Error</b> ".$aStr."<hr>\n");
  }
}
?>
