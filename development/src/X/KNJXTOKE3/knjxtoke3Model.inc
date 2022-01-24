<?php

require_once('for_php7.php');

//学習記録ｴｸｽﾌﾟﾛｰﾗ
class knjxtoke3Model extends model
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
    var $groupclasscd;      //グループ科目コード
    var $gradingcd;         //評価区分
    var $scheclasscd;        //時間割科目コード
    var $truncatecd;        //切り上げ区分
    var $disp;              //表示モード

    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    
    function knjxtoke3Model()
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
        if(VARS::request("PROGRAMID") !='') {
            $this->programid = VARS::request("PROGRAMID");
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        //権限が変更された場合
            $this->staffcd      = STAFFCD;                  //職員コード
        $this->cmd          = VARS::request("cmd");         //一覧表示時のcmd
        if(VARS::get("CLASSCD") !='') {
            $this->classcd      = VARS::get("CLASSCD");
        }
        if(VARS::request("SUBCLASSCD") !='') {
            $this->subclasscd    = explode(',', VARS::request("SUBCLASSCD"));
        }
        if(VARS::get("TESTKINDCD") != '') {
            $this->testkindcd   = VARS::get("TESTKINDCD");
        }
        if(VARS::get("TESTITEMCD") != '') {
            $this->testitemcd   = VARS::get("TESTITEMCD");
        }
        if(VARS::post("GROUPCD") != '') {
            $this->groupcd    = explode(',', VARS::post("GROUPCD"));
        }
        if(VARS::get("TITLE") != '') {
            $this->title        = VARS::get("TITLE");
        }
        if(VARS::post("ATTENDCLASSCD") != '') {
            $this->checked_attend    = explode(',', VARS::post("ATTENDCLASSCD"));
        }
        //年度、学期をPOSTより取得
        if(VARS::post("SEL_SEMI") !='') {
           list($this->year, $this->semester) = explode(",", VARS::post("SEL_SEMI"));
        }
        //グループ科目コードGETより取得
        if(VARS::get("GROUPCLASSCD") != '') {
            $this->groupclasscd        = VARS::get("GROUPCLASSCD");
        }
        //評価区分GETより取得
        if(VARS::get("GRADINGCD") != '') {
            $this->gradingcd        = VARS::get("GRADINGCD");
        }
        //時間割科目コードGETより取得
        if(VARS::get("SCHECLASSCD") != '') {
            $this->scheclasscd        = VARS::get("SCHECLASSCD");
        }
        //切り上げ区分GETより取得
        if(VARS::get("TRUNCATECD") != '') {
            $this->truncatecd        = VARS::get("TRUNCATECD");
        }
        //表示モード
        if(VARS::get("DISP") != '') {
            $this->disp             = VARS::get("DISP");
        }
        if(VARS::get("SEND_PRGID")){
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }

        //権限チェック
        $auth = common::SecurityCheck($this->staffcd, $this->programid);
        $this->auth = $this->getPrgId ? $this->sendAuth : $auth;

        if ($this->cmd == "init"){
            unset($this->classcd);           //教科コード
            unset($this->subclasscd);        //科目コード
            unset($this->testkindcd);        //テスト種別コード
            unset($this->testitemcd);        //テスト項目コード
            unset($this->checked_attend);    //チェックした受講クラスコード
            unset($this->title);                //タイトル
            unset($this->auth);                 //権限チェック
            unset($this->groupclasscd);         //グループ科目コード
            unset($this->gradingcd);            //評価区分
            unset($this->scheclasscd);           //時間割科目コード
            unset($this->truncatecd);            //切り上げ区分
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }
    //初期化
    function clear()
    {
        unset($this->cmd);              //一覧表示時のcmd
        unset($this->year);             //年度
        unset($this->semester);         //学期
//        unset($this->staffcd);         //職員コード
        unset($this->classcd);           //教科コード
        unset($this->subclasscd);        //科目コード
        unset($this->testkindcd);        //テスト種別コード
        unset($this->testitemcd);        //テスト項目コード
        unset($this->checked_attend);    //チェックした受講クラスコード
        unset($this->title);                //タイトル
//        unset($this->programid);          //プログラムID
        unset($this->auth);                 //権限チェック
        unset($this->groupclasscd);         //グループ科目コード
        unset($this->gradingcd);            //評価区分
        unset($this->scheclasscd);           //時間割科目コード
        unset($this->truncatecd);            //切り上げ区分
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
