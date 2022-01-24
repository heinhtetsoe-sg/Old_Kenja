<?php

require_once('for_php7.php');

//学習記録ｴｸｽﾌﾟﾛｰﾗ
class knja233Model extends model
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
    var $appdate;           //適用開始日付
    var $name_show;         //STAFFCD
    var $checked_attend;    //チェックした受講クラスコード
    var $groupcd;           //群コード
    var $title;             //タイトル
    var $programid;         //プログラムID
    var $auth;              //権限チェック
    var $control;           //コントロールマスタ
    var $groupclasscd;      //グループ科目コード
    var $disp;              //表示モード
    var $semi_start;        //学期開始日付
    var $semi_end;          //学期終了日付
    var $schoolName;
    var $schoolCode;

    var $schoolCd;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限

    function knja233Model()
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
        //プロパティファイルの読込み
        $this->getPropertiesAll();
        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),           //名票
                            "KENSUU"            =>  VARS::post("KENSUU")            //出力件数
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

        $this->cmd = VARS::request("cmd");         //一覧表示時のcmd

        $classcd    = VARS::post("CLASSCD");
        if (isset($classcd)){
            $this->classcd      = VARS::post("CLASSCD");
        }        
        $subclasscd = VARS::request("SUBCLASSCD");
        if (isset($subclasscd)){
            $this->subclasscd   = explode(',', VARS::request("SUBCLASSCD"));
        }
        $testkindcd = VARS::post("TESTKINDCD");
        if (isset($testkindcd)){
            $this->testkindcd   = VARS::post("TESTKINDCD");
        }
        $testitemcd = VARS::post("TESTITEMCD");
        if (isset($testitemcd)){
            $this->testitemcd   = VARS::post("TESTITEMCD");
        }
        $groupcd    = VARS::post("GROUPCD");
        if (isset($groupcd)){
            $this->groupcd      = explode(',', VARS::post("GROUPCD"));
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
        if (isset($attendclasscd)){
            $this->checked_attend    = explode(',', VARS::post("ATTENDCLASSCD"));
        }
        $year = VARS::request("YEAR");
        if (isset($year)){
            //年度を取得
            $this->year = VARS::request("YEAR");
        }
        $appdate = VARS::post("APPDATE");
        if (isset($appdate)){
            $this->appdate    = explode(',', VARS::post("APPDATE"));
        }
        $name_show = VARS::post("NAME_SHOW");
        if (isset($name_show)){
            $this->name_show    = explode(',', VARS::post("NAME_SHOW"));
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
        //表示モード
        $disp      = VARS::post("DISP");
        if (isset($disp)){
            $this->disp             = VARS::post("DISP");
        }

        //学校名取得
        $query = knja233Query::getSchoolName();
        $db = Query::dbCheckOut();
        $rowZ010 = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $this->schoolName = $rowZ010["NAME1"];
        $this->schoolCode = $rowZ010["NAME2"];
        Query::dbCheckIn($db);

        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        if(VARS::get("SEND_PRGID")){
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }

        //権限チェック
        $auth = common::SecurityCheck($this->staffcd, $this->programid);
        $auth = ($auth) ? $auth : AUTHORITY;
        $this->auth = $this->getPrgId ? $this->sendAuth : $auth;

        $this->getPropertiesAll(); //プロパティファイル

    }
    //初期化
    function clear()
    {
        unset($this->cmd);              //一覧表示時のcmd
        unset($this->year);             //年度
        unset($this->semester);         //学期
        unset($this->classcd);          //教科コード
        unset($this->subclasscd);       //科目コード
        unset($this->testkindcd);       //テスト種別コード
        unset($this->testitemcd);       //テスト項目コード
        unset($this->attendclasscd);    //受講クラスコード
        unset($this->checked_attend);   //チェックした受講クラスコード
        unset($this->groupcd);          //群コード
        unset($this->title);            //タイトル
        unset($this->auth);             //権限チェック
        unset($this->groupclasscd);     //グループ科目コード
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
        $this->knja233Model();
    }

    /**
     * CSV
     */
    function getDownloadModel()
    {
        //ヘッダの作成
        if ($this->field["OUTPUT"] == 1 || $this->field["OUTPUT"] == 'musashi'){
            $header = array("CHAIRNAMET"        => "講座名",
                            "CHAIRNAME"         => "講座名",
                            "STAFFNAMET"        => "担当者名",
                            "STAFFNAME"         => "担当者名"
                            );
        }else {
            $header = array("CHAIRCDT"          => "講座コード",
                            "CHAIRCD"           => "講座コード",
                            "CHAIRNAMET"        => "講座名",
                            "CHAIRNAME"         => "講座名",
                            "STAFFNAMET"        => "担当者名",
                            "STAFFNAME"         => "担当者名"
                            );
        }

        //項目作成
        if ($this->field["OUTPUT"] == 1 || $this->field["OUTPUT"] == 'musashi'){
            $meisai = array("SCHREGNO"          => "連番",
                            "SEX"               => "性別",
                            "NAME"              => "氏名",
                            "NAMEKANA"          => "かな",
                            "HR_CLASS"          => "年組",
                            "ATTENDNO"          => "出席番号"
                        );
        }else {
            $meisai = array("SCHREGNO"          => "学籍番号",
                            "SEX"               => "性別",
                            "NAME"              => "氏名",
                            "NAMEKANA"          => "かな",
                            "HR_CLASS"          => "年組",
                            "ATTENDNO"          => "出席番号"
                        );
        }

        $contents = "" ;

        //出力データの作成//
        for ($i = 0 ; $i < get_count($this->attendclasscd) ; $i++){
            $renban = 1 ;
            if ($i > 0){
                $contents .= "\n";
            }
            //講座名称取得
            $db = Query::dbCheckOut();
            $query = knja233Query::getCair($this->attendclasscd[$i], $this);
            $chairname = $db->getOne($query);
            Query::dbCheckIn($db);

            if ($this->field["OUTPUT"] == 1 || $this->field["OUTPUT"] == 'musashi'){
                $header["CHAIRNAME"] = $chairname;
            }else {
                $header["CHAIRCD"]   = $this->attendclasscd[$i];
                $header["CHAIRNAME"] = $chairname;
            }

            //担当者名称取得
            $db = Query::dbCheckOut();
            $query = knja233Query::getStaff($this->name_show[$i]);
            $staffname = $db->getOne($query);
            Query::dbCheckIn($db);
            $header["STAFFNAME"] = $staffname;

            //見出し設定
            $contents .= implode($header, ",") ."\n";
            $contents .= implode($meisai, ",") ."\n";

            //DB接続
            $db = Query::dbCheckOut();
            //SQL文発行
            $query = knja233Query::Csvdata_get($this->field["OUTPUT"], $this->attendclasscd[$i], $this->appdate[$i], $this);
            $result = $db->query($query);
            $data = false;
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                $row = str_replace(",",".",$row);
                if ($this->field["OUTPUT"] == 1 || $this->field["OUTPUT"] == 'musashi'){
                    $contents .= $renban.",";
                    $contents .= $row["SEX"].",";
                }else {
                    $contents .= $row["SCHREGNO"].",";
                    $contents .= $row["SEX"].",";
                }
                $contents .= $row["NAME"].",";
                $contents .= $row["NAME_KANA"].",";
                $contents .= $row["HR_NAMEABBV"].",";
                $contents .= $row["ATTENDNO"].","."\n";
                $data = true;
                $renban++;
            }
            $result->free();
            Query::dbCheckIn($db);
        }

        if ($data){
            if ($this->field["OUTPUT"] == 1){
                $couse = "講座別名票(年組番号順)";
            }elseif ($this->field["OUTPUT"] == 'musashi'){
                $couse = "講座別名票";
            }else {
                $couse = "講座別名票(学籍番号順)";
            }
            $filename = sprintf("%s.csv",
                                         $couse
                                );
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        }else{
            $this->setWarning("MSG303");
            return false;
        }
    }
}
?>
