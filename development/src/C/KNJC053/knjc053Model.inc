<?php

require_once('for_php7.php');

class knjc053Model extends Model
{
    public $field      = array();
    public $cmd;       //一覧表示時のcmd
    public $year;      //年度
    public $semester;  //学期
    public $staffcd;   //職員コード
    public $classcd;           //教科コード
    public $school_kind;       //学校校種
    public $curriculum_cd;     //教育課程
    public $subclasscd;        //科目コード
    public $attendclasscd;     //受講クラスコード
    public $checked_attend;    //チェックした受講クラスコード
    public $checked_staff;
    public $checked_appd;
    public $groupcd;           //群コード
    public $title;             //タイトル
    public $programid;         //プログラムID
    public $auth;              //権限チェック
    public $control;           //コントロールマスタ
    public $groupclasscd;      //グループ科目コード
    public $gradingcd;         //評価区分
    public $scheclasscd;       //時間割科目コード
    public $disp;              //表示モード
    public $semi_start;        //学期開始日付
    public $semi_end;          //学期終了日付
    public $sdate;             //開始日付
    public $edate;             //終了日付

    /**
     * メンバ変数の初期化
     */
    public function knjc053Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->control["学籍処理日"] = str_replace("-", "/", $this->control["学籍処理日"]);
    }
    public function init()
    {
        $this->field = array("DATE"             =>  VARS::post("DATE"),         //印刷範囲開始日
                             "DATE2"            =>  VARS::post("DATE2"),        //印刷範囲終了日
                             "OUTPUT"           =>  VARS::post("OUTPUT"),       //０校時を出力
                             "OUTPUT2"          =>  VARS::post("OUTPUT2"),      //ラジオ選択データ（累計種別）
//                             "OUTPUT3"        =>  VARS::post("OUTPUT3"),      //チェック選択
                             "OUTPUT4"          =>  VARS::post("OUTPUT4"),      //チェックボックス
                             "OUTPUT5"          =>  VARS::post("OUTPUT5"),      //チェックボックス
                             "TYUI_TYOUKA"      =>  VARS::post("TYUI_TYOUKA")   //注意・超過ラジオボタン
                     );
        $this->sdate = VARS::post("DATE");
        $this->edate = VARS::post("DATE2");
        //プログラムID
        $programid = VARS::get("PROGRAMID");
        if (isset($programid)) {
            $this->clear();    //初期化
            $this->programid = VARS::get("PROGRAMID");
        }
        //権限が変更された場合
        if ($this->staffcd != STAFFCD) {
            $this->clear();    //初期化
            $this->staffcd      = STAFFCD;                  //職員コード
        }
        $this->cmd = VARS::request("cmd");         //一覧表示時のcmd
        $classcd = VARS::request("CLASSCD");
        if (isset($classcd)) {
            $this->classcd    = explode(',', VARS::request("CLASSCD"));
        }

        $school_kind = VARS::request("SCHOOL_KIND");
        if (isset($school_kind)) {
            $this->school_kind = explode(',', VARS::request("SCHOOL_KIND"));
        }

        $curriculum_cd = VARS::request("CURRICULUM_CD");
        if (isset($curriculum_cd)) {
            $this->curriculum_cd = explode(',', VARS::request("CURRICULUM_CD"));
        }

        $subclasscd = VARS::request("SUBCLASSCD");
        if (isset($subclasscd)) {
            $this->subclasscd    = explode(',', VARS::request("SUBCLASSCD"));
        }

        $groupcd      = VARS::request("GROUPCD");
        if (isset($groupcd)) {
            $this->groupcd    = explode(',', VARS::request("GROUPCD"));
        }

        $title = VARS::request("TITLE");
        if (isset($title)) {
            $this->title        = VARS::request("TITLE");
        }

        $attendclasscd = VARS::request("ATTENDCLASSCD");
        if (isset($attendclasscd)) {
            $this->attendclasscd    = explode(',', VARS::request("ATTENDCLASSCD"));
        }
        $attendclasscd = VARS::request("ATTENDCLASSCD");
        if (isset($attendclasscd)) {
            $this->checked_attend    = explode(',', VARS::request("ATTENDCLASSCD"));
            $this->checked_staff    = VARS::post("NAME_SHOW");
            $this->checked_appd    = VARS::post("APPDATE");
        }

        $year = VARS::request("YEAR");
        if (isset($year)) {
            //年度を取得
            $this->year = VARS::request("YEAR");
        }

        $semi = VARS::request("SEMESTER");
        if (isset($semi)) {
            //学期を取得
            $this->semester = VARS::request("SEMESTER");
            $this->semi_start = $this->control["学期開始日付"][$this->semester];     //学期開始日付
            $this->semi_end   = $this->control["学期終了日付"][$this->semester];     //学期終了日付
        }

        //表示モード
        $disp      = VARS::get("DISP");
        if (isset($disp)) {
            $this->disp             = VARS::get("DISP");
        }

        //権限チェック
        $ret = common::SecurityCheck($this->staffcd, $this->programid);
        $this->auth = ($ret == DEF_UPDATABLE)? 1:0;   //更新可能

        $this->getPropertiesAll();
        //初期値の設定
        if (strlen($this->Properties["chikokuHyoujiFlg"])) {
            $this->Properties["chikokuHyoujiFlg"] = ($this->Properties["chikokuHyoujiFlg"] == '1') ? $this->Properties["chikokuHyoujiFlg"] : 0;
        } else {
            $this->Properties["chikokuHyoujiFlg"] = "";
        }
    }
    //初期化
    public function clear()
    {
        unset($this->cmd);              //一覧表示時のcmd
        unset($this->year);             //年度
        unset($this->semester);         //学期
        unset($this->classcd);          //教科コード
        unset($this->school_kind);      //学校校種
        unset($this->curriculum_cd);    //教育課程
        unset($this->subclasscd);       //科目コード
        unset($this->testkindcd);       //テスト種別コード
        unset($this->testitemcd);       //テスト項目コード
        unset($this->attendclasscd);    //受講クラスコード
        unset($this->checked_attend);   //チェックした受講クラスコード
        unset($this->checked_staff);
        unset($this->checked_appd);
        unset($this->groupcd);          //群コード
        unset($this->title);            //タイトル
        unset($this->auth);             //権限チェック
        unset($this->groupclasscd);     //グループ科目コード
        unset($this->gradingcd);        //評価区分
        unset($this->scheclasscd);      //時間割科目コード
        unset($this->truncatecd);       //切り上げ区分
    }

    /**
     * 読込のモデルを返します
    */
    public function &getReadModel()
    {
        $this->clear();
    }
    /**
     * ツリー表示用のモデルを返します
    */
    public function &getTreeModel()
    {
    }
    /**
     * 一覧表示用のモデルを返します
    */
    public function &getMainModel()
    {
        $this->lbb061Model();
    }
}
