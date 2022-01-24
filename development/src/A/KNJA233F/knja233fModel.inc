<?php

require_once('for_php7.php');

class knja233fModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $auth;      //権限
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $schoolName;
    var $schoolCode;

    function knja233fModel() {
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
        $this->auth = $this->getPrgId ? $this->sendAuth : common::SecurityCheck(STAFFCD, 'KNJA233F');

        $this->year = CTRL_YEAR;
        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "CLASSCD"          =>  VARS::post("CLASSCD"),          //教科コード
                             "SUBCLASSCD"       =>  VARS::post("SUBCLASSCD"),       //科目コード
                             "GRD_DIV"          =>  VARS::post("GRD_DIV"),          //転学・退学した生徒は出力しない
                             "DATE"             =>  VARS::post("DATE"),             //学籍処理日
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        if (!$this->field["DATE"]) {
            $this->field["DATE"] = $this->control["学籍処理日"];
        }

        $this->cmd = VARS::request("cmd");

        //学校名取得
        $query = knja233fQuery::getSchoolName();
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
                        "NAME"              => "氏名",
                        "NAME_KANA"         => "ふりがな"
                        );

        $contents = "";
        $contents = ",名列\n";

        //DB接続
        $db = Query::dbCheckOut();

        //出力データの作成
        for ($i = 0 ; $i < get_count($this->attendclasscd) ; $i++) {
            if ($i > 0) {
                $contents .= "\n";
            }

            //講座名称取得
            $query = knja233fQuery::getChairName($this->attendclasscd[$i], $this);
            $chairname = $db->getOne($query);
            $header["CHAIRNAME"] = $chairname;

            //担当者名称取得
            $query = knja233fQuery::getStaff($this->name_show[$i]);
            $staffname = $db->getOne($query);
            $header["STAFFNAME"] = $staffname;

            //見出し設定
            $contents .= implode($header, ",") ."\n";
            $contents .= implode($meisai, ",") ."\n";

            $line = 1;

            //SQL文発行
            $query = knja233fQuery::Csvdata_get($this->field["OUTPUT"], $this->attendclasscd[$i], $this->appdate[$i], $this);
            $result = $db->query($query);
            $data = false;
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                $row = str_replace(",",".",$row);
                
                while($line != intval($row["ATTENDNO"])) {
                    //出席番号の歯抜け行を挿入
                    $contents .= ",,,\n";
                    $line = $line + 1;
                }

                $sep = "";
                foreach ($meisai as $key => $val) {
                    if($key == "HR_CLASS"){
                        $contents .= $sep.ltrim($row["HR_CLASS"],'0');
                    } elseif($key == "ATTENDNO"){
                        $contents .= $sep.ltrim($row["ATTENDNO"],'0');
                    } elseif($key == "NAME"){
                        $contents .= $sep.$row["SEX"].$row["NAME"];
                    } else{
                        $contents .= $sep.$row[$key];
                    }
                    $sep = ',';
                }
                $contents .= "\n";
                $data = true;
                $line = $line + 1;
            }
            $result->free();
        }

        //DB切断
        Query::dbCheckIn($db);

        if ($data) {
            $title = "講座名簿";
            $filename = sprintf("%s.csv", $title);
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
