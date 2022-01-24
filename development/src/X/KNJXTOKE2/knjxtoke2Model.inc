<?php

require_once('for_php7.php');

//学習記録ｴｸｽﾌﾟﾛｰﾗ
class knjxtoke2Model extends model
{
    var $cmd;       //一覧表示時のcmd
    var $year;      //年度
    var $semester;  //学期
    var $staffcd;   //職員コード
    var $classcd;           //教科コード
    var $subclasscd;        //科目コード
    var $testkindcd;        //テスト種別コード
    var $testitemcd;        //テスト項目コード
    var $attendclasscd;     //受講クラスコード
    var $checked_attend;    //チェックした受講クラスコード
    var $groupcd;           //群コード
    var $title;             //タイトル
    var $programid;         //プログラムID
    var $auth;              //権限チェック
    var $control;           //コントロールマスタ
    var $groupclasscd;      //グループ科目コード
    var $gradingcd;         //評価区分
    var $disp;              //表示モード
    
    function knjxtoke2Model()
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
        $programid = VARS::get("PROGRAMID");
        if (isset($programid)){
            $this->clear();    //初期化
            $this->programid = VARS::get("PROGRAMID");
        }
        //権限が変更された場合
        if ($this->staffcd != STAFFCD){
            $this->clear();    //初期化
            $this->staffcd      = STAFFCD;                  //職員コード
        }
        $this->cmd          = VARS::request("cmd");         //一覧表示時のcmd
        $classcd      = VARS::get("CLASSCD");
        if (isset($classcd)){
            $this->classcd      = VARS::get("CLASSCD");
        }        
        $subclasscd   = VARS::request("SUBCLASSCD");
        if (isset($subclasscd)){
            $this->subclasscd    = explode(',', VARS::request("SUBCLASSCD"));
        }
        $testkindcd   = VARS::get("TESTKINDCD");
        if (isset($testkindcd)){
            $this->testkindcd   = VARS::get("TESTKINDCD");
        }
        $testitemcd   = VARS::get("TESTITEMCD");
        if (isset($testitemcd)){
            $this->testitemcd   = VARS::get("TESTITEMCD");
        }
        $groupcd      = VARS::post("GROUPCD");
        if (isset($groupcd)){
            $this->groupcd    = explode(',', VARS::post("GROUPCD"));
        }
        $title = VARS::get("TITLE");
        if (isset($title)){
            $this->title        = VARS::get("TITLE");
        }
        $attendclasscd = VARS::get("ATTENDCLASSCD");
        if (isset($attendclasscd)){
            $this->attendclasscd    = explode(',', VARS::get("ATTENDCLASSCD"));
        }
        $attendclasscd = VARS::post("ATTENDCLASSCD");
        if (isset($attendclasscd)){
            $this->checked_attend    = explode(',', VARS::post("ATTENDCLASSCD"));
        }
        $semi = VARS::request("SEL_SEMI");
        if (isset($semi)){
            //年度、学期をPOSTより取得
            list($this->year, $this->semester) = explode(",", VARS::request("SEL_SEMI"));
        }
        $groupclasscd = VARS::get("GROUPCLASSCD");
        if (isset($groupclasscd)){
            //グループ科目コードGETより取得
            $this->groupclasscd        = VARS::get("GROUPCLASSCD");
        }
        $gradingcd = VARS::get("GRADINGCD");
        if (isset($gradingcd)){
            //評価区分GETより取得
            $this->gradingcd        = VARS::get("GRADINGCD");
        }
        $scheclasscd = VARS::get("SCHECLASSCD");
        if (isset($scheclasscd)){
            //時間割科目コードGETより取得
            $this->scheclasscd        = VARS::get("SCHECLASSCD");
        }
        $truncatecd = VARS::get("TRUNCATECD");
        if (isset($truncatecd)){
            //切り上げ区分GETより取得
            $this->truncatecd        = VARS::get("TRUNCATECD");
        }
        //表示モード
        $disp      = VARS::get("DISP");
        if (isset($disp)){
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
        unset($this->attendclasscd);     //受講クラスコード
        unset($this->checked_attend);    //チェックした受講クラスコード
        unset($this->groupcd);           //群コード
        unset($this->title);                //タイトル
        unset($this->groupclasscd);         //グループ科目コード
        unset($this->gradingcd);            //評価区分
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
