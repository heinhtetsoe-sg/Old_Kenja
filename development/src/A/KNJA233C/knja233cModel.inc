<?php

require_once('for_php7.php');

class knja233cModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $schoolName;
    var $schoolCode;

    var $schoolCd;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限

    function knja233cModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
        $this->staffcd      = STAFFCD;                  //職員コード
    }

    function init() {
        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

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
        $this->auth = $this->getPrgId ? $this->sendAuth : common::SecurityCheck(STAFFCD, 'KNJA233C');

        $this->getPropertiesAll(); //プロパティファイル

        $this->year = CTRL_YEAR;
        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "CLASSCD"          =>  VARS::post("CLASSCD"),          //教科コード
                             "SUBCLASSCD"       =>  VARS::post("SUBCLASSCD"),       //科目コード
                             "PRINT_DIV"        =>  VARS::post("PRINT_DIV"),        //帳票種別　1:講座名簿（OUTPUT=1,2,3）、2:教務手帳（OUTPUT=4）
                             "PATTERN"          =>  VARS::post("PATTERN"),          //講座名簿AB
                             "OUTPUT"           =>  VARS::post("OUTPUT"),           //名票
                             "OUTPUT4AB"        =>  VARS::post("OUTPUT4AB"),        //教務手帳AB
                             "HURIGANA_OUTPUT"  =>  VARS::post("HURIGANA_OUTPUT"),  //ふりがな出力
                             "HEIGHT"           =>  VARS::post("HEIGHT"),           //縦サイズ
                             "WIDTH"            =>  VARS::post("WIDTH"),            //横サイズ
                             "PRINT_SCHREGNO"   =>  VARS::post("PRINT_SCHREGNO"),   //学籍番号を表記する
                             "KENSUU"           =>  VARS::post("KENSUU"),           //出力件数
                             "DATE"             =>  VARS::post("DATE"),             //学籍処理日
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        if (!$this->field["DATE"]) {
            $this->field["DATE"] = $this->control["学籍処理日"];
        }

        $this->cmd = VARS::request("cmd");

        //学校名取得
        $query = knja233cQuery::getSchoolName();
        $db = Query::dbCheckOut();
        $rowZ010 = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $this->schoolName = $rowZ010["NAME1"];
        $this->schoolCode = $rowZ010["NAME2"];
        Query::dbCheckIn($db);
    }

    /*******************************************************************************/
    /*******************************************************************************/
    /*******************************************************************************/
    /*******************************************************************************/
    /*******************************************************************************/
    //CSVの出力
    function getDownloadModel() {
        $attendclasscd = VARS::post("ATTENDCLASSCD");
        if (isset($attendclasscd)) {
            $this->attendclasscd    = explode(',', VARS::post("ATTENDCLASSCD"));
        }

        $groupcd = VARS::post("GROUPCD");
        if (isset($groupcd)) {
            $this->groupcd    = explode(',', VARS::post("GROUPCD"));
        }

        $name_show = VARS::post("NAME_SHOW");
        if (isset($name_show)) {
            $this->name_show    = explode(',', VARS::post("NAME_SHOW"));
        }

        $chargediv = VARS::post("CHARGEDIV");
        if (isset($chargediv)) {
            $this->chargediv    = explode(',', VARS::post("CHARGEDIV"));
        }

        $appdate = VARS::post("APPDATE");
        if (isset($appdate)) {
            $this->appdate    = explode(',', VARS::post("APPDATE"));
        }

        //ヘッダの作成
        if ($this->field["OUTPUT"] == 1 || $this->field["OUTPUT"] == 'musashi'){
            $header = array("CHAIRNAMET"        => "講座名",
                            "CHAIRNAME"         => "講座名",
                            "STAFFNAMET"        => "担当者名",
                            "STAFFNAME"         => "担当者名"
                            );
        } else {
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
            if ($this->field["PRINT_DIV"] == 1 && $this->field["PATTERN"] == 2) {
                $meisai = array("SCHREGNO"          => "連番",
                                "GRADE"             => "学年",
                                "HR_CLASS"          => "組",
                                "ATTENDNO"          => "出席番号",
                                "SEX"               => "性別",
                                "NAME"              => "氏名",
                                "NAMEKANA"          => "かな"
                            );
            } else {
                $meisai = array("SCHREGNO"          => "連番",
                                "SEX"               => "性別",
                                "NAME"              => "氏名",
                                "NAMEKANA"          => "かな",
                                "HR_CLASS"          => "年組",
                                "ATTENDNO"          => "出席番号"
                            );
            }
        }else {
            if ($this->field["PRINT_DIV"] == 1 && $this->field["PATTERN"] == 2) {
                $meisai = array("SCHREGNO"          => "学籍番号",
                                "GRADE"             => "学年",
                                "HR_CLASS"          => "組",
                                "ATTENDNO"          => "出席番号",
                                "SEX"               => "性別",
                                "NAME"              => "氏名",
                                "NAMEKANA"          => "かな"
                            );
            } else {
                $meisai = array("SCHREGNO"          => "学籍番号",
                                "SEX"               => "性別",
                                "NAME"              => "氏名",
                                "NAMEKANA"          => "かな",
                                "HR_CLASS"          => "年組",
                                "ATTENDNO"          => "出席番号"
                            );
            }
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
            $query = knja233cQuery::getCair($this->attendclasscd[$i], $this);
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
            $query = knja233cQuery::getStaff($this->name_show[$i]);
            $staffname = $db->getOne($query);
            Query::dbCheckIn($db);
            $header["STAFFNAME"] = $staffname;

            //見出し設定
            $contents .= implode($header, ",") ."\n";
            $contents .= implode($meisai, ",") ."\n";

            //DB接続
            $db = Query::dbCheckOut();
            //SQL文発行
            $query = knja233cQuery::Csvdata_get($this->field["OUTPUT"], $this->attendclasscd[$i], $this->appdate[$i], $this);
            $result = $db->query($query);
            $data = false;
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                $row = str_replace(",",".",$row);
                if ($this->field["OUTPUT"] == 1 || $this->field["OUTPUT"] == 'musashi'){
                    $contents .= $renban.",";
                }else {
                    $contents .= $row["SCHREGNO"].",";
                }
                if ($this->field["PRINT_DIV"] == 1 && $this->field["PATTERN"] == 2) {
                    $contents .= $row["GRADE"].",".$row["HR_CLASS"].",";
                    $contents .= $row["ATTENDNO"].",";
                }
                $contents .= $row["SEX"].",";
                $contents .= $row["NAME"].",";
                $contents .= $row["NAME_KANA"].",";
                if ($this->field["PRINT_DIV"] == 1 && $this->field["PATTERN"] == 2) {
                } else {
                    $contents .= $row["HR_NAMEABBV"].",";
                    $contents .= $row["ATTENDNO"].",";
                }
                $contents .= "\n";
                $data = true;
                $renban++;
            }
            $result->free();
            Query::dbCheckIn($db);
        }

        if ($data){
            if ($this->field["OUTPUT"] == 2){
                $couse = "講座別名票(学籍番号順)";
            }elseif ($this->field["OUTPUT"] == 'musashi'){
                $couse = "講座別名票";
            }else {
                $couse = "講座別名票(年組番号順)";
            }
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        }else{
            $this->setWarning("MSG303");
            return false;
        }
    }

    function clean() {}
}
?>