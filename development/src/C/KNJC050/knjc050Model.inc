<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjc050Model extends Model {

    var $field      = array();
    var $cmd;       //一覧表示時のcmd
    var $year;      //年度
    var $semester;  //学期
    var $staffcd;   //職員コード
    var $classcd;           //教科コード
    var $subclasscd;        //科目コード
    var $attendclasscd;     //受講クラスコード
    var $checked_attend;    //チェックした受講クラスコード
    var $checked_staff;    	//2004-08-11 naka
    var $checked_appd;    	//2004-08-11 naka
    var $groupcd;           //群コード
    var $title;             //タイトル
    var $programid;         //プログラムID
    var $auth;              //権限チェック
    var $control;           //コントロールマスタ
    var $groupclasscd;      //グループ科目コード
    var $gradingcd;         //評価区分
    var $scheclasscd;        //時間割科目コード
    var $disp;              //表示モード
    var $semi_start;        //学期開始日付
    var $semi_end;          //学期終了日付
        
    /**
     * メンバ変数の初期化
     */
    function knjc050Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
//        $this->semi_start = $this->control["学期開始日付"][$this->control["学期"]];     //学期開始日付
//        $this->semi_end   = $this->control["学期終了日付"][$this->control["学期"]];     //学期終了日付
    }
    function init()
    {               
        $this->field = array("DATE"             =>  VARS::post("DATE"),       //印刷範囲開始日
                            "DATE2"            =>  VARS::post("DATE2"),      //印刷範囲終了日
                            "OUTPUT"           =>  VARS::post("OUTPUT"),     //０校時を出力
                            "OUTPUT2"          =>  VARS::post("OUTPUT2"),     //ラジオ選択データ（累計種別）
                            "OUTPUT3"          =>  VARS::post("OUTPUT3")     //チェック選択
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
        $classcd      = VARS::request("CLASSCD");
        if (isset($classcd)){
            $this->classcd    = explode(',', VARS::request("CLASSCD"));
        }
        $subclasscd   = VARS::request("SUBCLASSCD");

        if (isset($subclasscd)){
            $this->subclasscd    = explode(',', VARS::request("SUBCLASSCD"));
        }

        $groupcd      = VARS::request("GROUPCD");
        if (isset($groupcd)){
            $this->groupcd    = explode(',', VARS::request("GROUPCD"));
        }

        $title = VARS::request("TITLE");
        if (isset($title)){
            $this->title        = VARS::request("TITLE");
        }

        $attendclasscd = VARS::request("ATTENDCLASSCD");
        if (isset($attendclasscd)){
            $this->attendclasscd    = explode(',', VARS::request("ATTENDCLASSCD"));
        }
        $attendclasscd = VARS::request("ATTENDCLASSCD");
        if (isset($attendclasscd)){
            $this->checked_attend    = explode(',', VARS::request("ATTENDCLASSCD"));
            $this->checked_staff    = VARS::post("NAME_SHOW");	//2004-08-11 naka
            $this->checked_appd    = VARS::post("APPDATE");		//2004-08-11 naka
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

        //表示モード
        $disp      = VARS::get("DISP");
        if (isset($disp)){
            $this->disp             = VARS::get("DISP");
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
        unset($this->classcd);           //教科コード
        unset($this->subclasscd);        //科目コード
        unset($this->testkindcd);        //テスト種別コード
        unset($this->testitemcd);        //テスト項目コード
        unset($this->attendclasscd);     //受講クラスコード
        unset($this->checked_attend);    //チェックした受講クラスコード
        unset($this->checked_staff);    //2004-08-11 naka
        unset($this->checked_appd);    	//2004-08-11 naka
        unset($this->groupcd);           //群コード
        unset($this->title);                //タイトル
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
        $this->lbb061Model();
    }
}
?>
