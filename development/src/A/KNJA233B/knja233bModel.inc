<?php

require_once('for_php7.php');

class knja233bModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $auth;      //権限
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $schoolName;
    var $schoolCode;

    function knja233bModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
    }

    function init() {

        $this->getPropertiesAll(); //プロパティファイル

        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        if (VARS::get("SEND_PRGID")) {
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }

        //権限チェック
        $this->auth = $this->getPrgId ? $this->sendAuth : common::SecurityCheck(STAFFCD, 'KNJA233B');

        $this->year = CTRL_YEAR;
        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "CLASSCD"          =>  VARS::post("CLASSCD"),          //教科コード
                             "SUBCLASSCD"       =>  VARS::post("SUBCLASSCD"),       //科目コード
                             "KENSUU"           =>  VARS::post("KENSUU"),           //出力件数
                             "OUTPUTLANG"       =>  VARS::post("OUTPUTLANG"),       //出力言語(日/英)
                             "GRD_DIV"          =>  VARS::post("GRD_DIV"),          //転学・退学した生徒は出力しない
                             "DATE"             =>  VARS::post("DATE"),             //学籍処理日
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        if (!$this->field["DATE"]) {
            $this->field["DATE"] = $this->control["学籍処理日"];
        }

        $this->cmd = VARS::request("cmd");

        //学校名取得
        $query = knja233bQuery::getSchoolName();
        $db = Query::dbCheckOut();
        $rowZ010 = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $this->schoolName = $rowZ010["NAME1"];
        $this->schoolCode = $rowZ010["NAME2"];
        Query::dbCheckIn($db);
    }

    /*************/
    /* CSVの出力 */
    /*************/
    function getDownloadModel() {
        //講座コード
        $attendclasscd = VARS::post("ATTENDCLASSCD");
        if (isset($attendclasscd)) {
            $this->attendclasscd = explode(',', VARS::post("ATTENDCLASSCD"));
        }
        //グループコード
        $groupcd = VARS::post("GROUPCD");
        if (isset($groupcd)) {
            $this->groupcd = explode(',', VARS::post("GROUPCD"));
        }
        //職員コード（講座担当）
        $name_show = VARS::post("NAME_SHOW");
        if (isset($name_show)) {
            $this->name_show = explode(',', VARS::post("NAME_SHOW"));
        }
        //担任区分
        $chargediv = VARS::post("CHARGEDIV");
        if (isset($chargediv)) {
            $this->chargediv    = explode(',', VARS::post("CHARGEDIV"));
        }
        //名簿の適用開始日付
        $appdate = VARS::post("APPDATE");
        if (isset($appdate)) {
            $this->appdate    = explode(',', VARS::post("APPDATE"));
        }

        //ヘッダの作成
        $header = array("CHAIRNAMET"        => "講座名",
                        "CHAIRNAME"         => "",
                        "STAFFNAMET"        => "担当者名",
                        "STAFFNAME"         => ""
                        );

        //項目作成
        $meisai = array("HR_CLASS"          => "組",
                        "ATTENDNO"          => "番号",
                        "SEX"               => "性別",
                        "NAME"              => "氏名"
                        );

        $contents = "" ;

        //DB接続
        $db = Query::dbCheckOut();

        //出力データの作成
        for ($i = 0 ; $i < get_count($this->attendclasscd) ; $i++) {
            if ($i > 0) {
                $contents .= "\n";
            }
            //講座名称取得
            $query = knja233bQuery::getChairName($this->attendclasscd[$i], $this);
            $chairname = $db->getOne($query);
            $header["CHAIRNAME"] = $chairname;

            //担当者名称取得
            $query = knja233bQuery::getStaff($this->name_show[$i]);
            $staffname = $db->getOne($query);
            $header["STAFFNAME"] = $staffname;

            //見出し設定
            $contents .= implode($header, ",") ."\n";
            $contents .= implode($meisai, ",") ."\n";

            //SQL文発行
            $query = knja233bQuery::Csvdata_get($this->field["OUTPUT"], $this->attendclasscd[$i], $this->appdate[$i], $this);
            $result = $db->query($query);
            $data = false;
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                $row = str_replace(",",".",$row);

                $sep = "";
                foreach ($meisai as $key => $val) {
                    $contents .= $sep.$row[$key];
                    $sep = ',';
                }
                $contents .= "\n";
                $data = true;
            }
            $result->free();
        }

        //DB切断
        Query::dbCheckIn($db);

        if ($data) {
            $couse = "講座別名票";
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }

    function clean() {}
}
?>
