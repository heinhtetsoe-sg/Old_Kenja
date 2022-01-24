<?php

require_once('for_php7.php');

//学習記録ｴｸｽﾌﾟﾛｰﾗ
class knjxtoke4Model extends model
{
    var $cmd;       //一覧表示時のcmd
    var $year;      //年度
    var $semester;  //学期
    var $staffcd;   //職員コード
    var $classcd;           //教科コード
    var $subclasscd;        //科目コード
    var $testkindcd;        //テスト種別コード
    var $testitemcd;        //テスト項目コード
    var $groupcd;           //群コード
    var $checked_attend;    //チェックした受講クラスコード
    var $title;             //タイトル
    var $programid;         //プログラムID
    var $auth;              //権限チェック
    var $control;           //コントロールマスタ
    var $disp;              //表示モード

    function knjxtoke4Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->year = $this->control["年度"];
        $this->semester = $this->control["学期"];
    }
    function init()
    {
        //プログラムID
        if (VARS::request("PROGRAMID") != ''){
            $this->clear();    //初期化
            $this->programid = VARS::request("PROGRAMID");
        }
        //権限が変更された場合
        if ($this->staffcd != STAFFCD){
            $this->clear();    //初期化
            $this->staffcd      = STAFFCD;                  //職員コード
        }
        $this->cmd          = VARS::request("cmd");         //一覧表示時のcmd
        if (VARS::get("CLASSCD") != ''){
            $this->classcd      = VARS::get("CLASSCD");
        }
        if (VARS::request("SUBCLASSCD") != ''){
            $this->subclasscd    = explode(',', VARS::request("SUBCLASSCD"));
        }
        if (VARS::get("TESTKINDCD") != ''){
            $this->testkindcd   = VARS::get("TESTKINDCD");
        }
        if (VARS::get("TESTITEMCD") != ''){
            $this->testitemcd   = VARS::get("TESTITEMCD");
        }
        if (VARS::post("GROUPCD") != ''){
            $this->groupcd    = explode(',', VARS::post("GROUPCD"));
        }
        if (VARS::get("TITLE") != ''){
            $this->title        = VARS::get("TITLE");
        }
        if (VARS::post("ATTENDCLASSCD") != ''){
            $this->checked_attend    = explode(',', VARS::post("ATTENDCLASSCD"));
        }
        if (VARS::post("SEL_SEMI") != ''){
            //年度、学期をPOSTより取得
            list($this->year, $this->semester) = explode(",", VARS::post("SEL_SEMI"));
        }
        //表示モード
        if (VARS::get("DISP") != ''){
            $this->disp             = VARS::get("DISP");
        }
        //権限チェック
        $this->auth = common::SecurityCheck($this->staffcd, $this->programid);

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }
    //初期化
    function clear()
    {
        unset($this->cmd);              //一覧表示時のcmd
        unset($this->classcd);           //教科コード
        unset($this->subclasscd);        //科目コード
        unset($this->testkindcd);        //テスト種別コード
        unset($this->testitemcd);        //テスト項目コード
        unset($this->checked_attend);    //チェックした受講クラスコード
        unset($this->title);                //タイトル
//        unset($this->programid);          //プログラムID
    }
    /**
     * 読込のモデルを返します
    */
    function &getReadModel()
    {
        $this->clear();
    }
    /**
     * ツリー表示用のモデルを返します
    */
    function &getTreeModel()
    {
    }
    /**
     * 一覧表示用のモデルを返します
    */
    function &getMainModel()
    {
    }
}
?>
