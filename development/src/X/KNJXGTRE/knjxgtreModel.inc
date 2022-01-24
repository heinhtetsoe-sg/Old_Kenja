<?php

require_once('for_php7.php');

//学習記録ｴｸｽﾌﾟﾛｰﾗ
class knjxgtreModel extends model
{
    var $cmd;                // 一覧表示時のcmd
    var $programid;          // プログラムID
    var $disp;               // 表示モード
    var $appd;               // 適用開始日付の表示フラグ
    var $title;              // タイトル
    var $semester;           // 学期
    var $chaircd;            // 講座コード
    var $subclasscd;         // 科目コード
    var $testitemcd;         // テスト項目
    var $testkindcd;         // テスト種別
    var $appdate;            // 適用開始日付

    function knjxgtreModel()
    {
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->year         = CTRL_YEAR;                //年度
        $this->semester     = CTRL_SEMESTER;            //学期
    }
    function init()
    {
        //プログラムID
        if (VARS::get("PROGRAMID")){
            $this->clear();    //初期化
            $this->programid = VARS::get("PROGRAMID");
            $this->disp      = VARS::get("DISP");
            $this->appd      = VARS::get("APPD");
        }
        if (VARS::request("SEMESTER")){
            $this->semester = VARS::request("SEMESTER");   //学期
        }
        if (VARS::get("CLASSCD")){
            $this->subclasscd = VARS::get("CLASSCD");      //教科
        }
        if (VARS::get("SUBCLASSCD")){
            $this->subclasscd = VARS::get("SUBCLASSCD");   //科目
        }
        if (VARS::get("TESTITEMCD")){
            $this->testitemcd = VARS::get("TESTITEMCD");   //テスト項目
        }
        if (VARS::get("TESTKINDCD")){
            $this->testkindcd = VARS::get("TESTKINDCD");   //テスト種別
        }
        if ($this->disp != "TEST"){
            unset($this->testitemcd);
            unset($this->testkindcd);
        }
        if (VARS::get("TITLE")){
            $this->title = VARS::get("TITLE");             //タイトル
        }
        //学習エクスプローラより送信されたデータ
        if(VARS::request("CHAIRCD")){
            $this->chaircd = explode(",", VARS::request("CHAIRCD"));
        }

        if(VARS::post("APPDATE") != ""){
            $this->appdate = explode(",", VARS::post("APPDATE"));
        }
        
        $this->cmd      = VARS::request("cmd");      //一覧表示時のcmd
        //権限チェック
        $this->auth = common::SecurityCheck($this->staffcd, $this->programid);
        //変数初期化
        if ($this->cmd == "init"){
            $this->clear();
		}
    }
    //初期化
    function clear()
    {
        unset($this->chaircd);
        unset($this->subclasscd);
        unset($this->title);
        unset($this->appdate);
    }
}
?>
