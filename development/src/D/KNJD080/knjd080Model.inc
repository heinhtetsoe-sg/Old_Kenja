<?php

require_once('for_php7.php');

//学習記録ｴｸｽﾌﾟﾛｰﾗ
class knjd080Model extends model
{
    var $field      = array();
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
    var $checked_staff;    //チェックした職員コード			//	2004-07-30 naka
    var $groupcd;           //群コード
    var $title;             //タイトル
    var $programid;         //プログラムID
    var $auth;              //権限チェック
    var $control;           //コントロールマスタ
    var $groupclasscd;      //グループ科目コード
    var $gradingcd;         //評価区分
    var $scheclasscd;        //時間割科目コード
    var $truncatecd;        //切り上げ区分
    var $disp;              //表示モード
    var $semi_start;        //学期開始日付
    var $semi_end;          //学期終了日付
    
    function knjd080Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->year = $this->control["年度"];
        $this->semester = $this->control["学期"];
    }
    function init()
    {
        $this->field = array("TEXT"			 =>  VARS::post("TEXT"),            //席次
        					"OUT2"           =>  VARS::post("OUT2"),            //出力形式
                     );
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
        $classcd      = VARS::post("CLASSCD");
        if (isset($classcd)){
            $this->classcd      = VARS::post("CLASSCD");
        }        
        $subclasscd   = VARS::request("SUBCLASSCD");
        if (isset($subclasscd)){
            $this->subclasscd    = explode(',', VARS::request("SUBCLASSCD"));
        }
        $testkindcd   = VARS::post("TESTKINDCD");
        if (isset($testkindcd)){
            $this->testkindcd   = VARS::post("TESTKINDCD");
        }
        $testitemcd   = VARS::post("TESTITEMCD");
        if (isset($testitemcd)){
            $this->testitemcd   = VARS::post("TESTITEMCD");
        }
        $groupcd      = VARS::post("GROUPCD");
        if (isset($groupcd)){
            $this->groupcd    = explode(',', VARS::post("GROUPCD"));
        }
        $title = VARS::get("TITLE");
        if (isset($title)){
            $this->title        = VARS::request("TITLE");
        }
        $attendclasscd = VARS::post("ATTENDCLASSCD");
        if (isset($attendclasscd)){
            $this->attendclasscd    = explode(',', VARS::post("ATTENDCLASSCD"));
        }
        $attendclasscd = VARS::post("ATTENDCLASSCD");
        if ($attendclasscd!=""){
            $this->checked_attend    = explode(',', VARS::post("ATTENDCLASSCD"));
        }
	//	2004-07-30 naka
        if ($attendclasscd!=""){
            $this->checked_staff    = explode(',', VARS::post("STAFF_CD"));
        }
        $year = VARS::request("YEAR");
        if (isset($year)){
            //年度を取得
            $this->year = VARS::request("YEAR");
        }
        $semi = VARS::request("SEMESTER");
        if (isset($semi)){
            //学期を取得
            $this->semester = VARS::request("SEMESTER");
            $this->semi_start = $this->control["学期開始日付"][$this->semester];     //学期開始日付
            $this->semi_end   = $this->control["学期終了日付"][$this->semester];     //学期終了日付
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
        $disp      = VARS::post("DISP");
        if (isset($disp)){
            $this->disp             = VARS::post("DISP");
        }

        //権限チェック
        $ret = common::SecurityCheck($this->staffcd, $this->programid);
        $this->auth = ($ret == DEF_UPDATABLE)? 1:0;   //更新可能

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
        unset($this->attendclasscd);     //受講クラスコード
        unset($this->checked_attend);    //チェックした受講クラスコード
        unset($this->checked_staff);   //チェックした職員コード			//	2004-07-30 naka
        unset($this->groupcd);           //群コード
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
        $this->knjd080Model();
    }
}
?>