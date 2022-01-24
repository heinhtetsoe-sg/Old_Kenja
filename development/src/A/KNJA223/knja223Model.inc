<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knja223Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $staffcd;        //職員コード
    var $selectdata;
    var $schoolCd;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限

    /**
     * メンバ変数の初期化
     */
    function knja223Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //職員コード
    }

    function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        if (VARS::get("SEND_PRGID")) {
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        $this->field = array("YEAR"             =>  VARS::post("YEAR"),             //年度
                            "OUTPUT"            =>  VARS::post("OUTPUT"),           //名票
                            "OUTPUT2"           =>  VARS::post("OUTPUT2"),          //空行を詰めて印字
                            "KENSUU"            =>  VARS::post("KENSUU"),           //出力件数
                            "GAKKI"             =>  VARS::post("GAKKI"),            //学期コード
                            "DBNAME"            =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->selectdata  = VARS::post("selectdata");

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    // ＣＳＶ出力
    function getDownloadModel()
    {
        $header1 = array("HR_NAMEABBV"      => "",
                         "BLANK"            => "",
                         "STAFFNAME"        => "");
        if ($this->field["OUTPUT"] == "1") {
            $header2 = array("ATTENDNO"         => "出席番号",
                             "SEX"              => "性別",
                             "NAME"             => "氏名",
                             "NAME_KANA"        => "かな");
        } else {
            $header2 = array("ATTENDNO"         => "出席番号",
                             "SEX"              => "性別",
                             "NAME"             => "氏名");
        }

        $db = Query::dbCheckOut();

        $gyo_no = 1;
        $grade_hr_class = "";
        $header_data = false;
        $contents = $contents2 = "";

        $query = knja223Query::selectCsvQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            // データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace(",", ".", $row);
            // 年組コードのブレイク時
            if ($grade_hr_class != $row["GRADE_HR_CLASS"]) {
                if ($grade_hr_class != "") {
                    for ($i = 0; $i < $this->field["KENSUU"]; $i++) $contents .= $contents2 ."\n";
                    $contents2 = "";
                }
                $gyo_no = 1;
                $header1["HR_NAMEABBV"] = "年組：" .$row["HR_NAMEABBV"];
                $header1["STAFFNAME"] = "担任名：" .$row["STAFFNAME"];
                $contents2 .= implode($header1, ",")."\n";
                $contents2 .= implode($header2, ",")."\n";
                $grade_hr_class = $row["GRADE_HR_CLASS"];
            }
            // 空行を出力
            if ($this->field["OUTPUT2"] != "1") {
                while ($gyo_no < $row["ATTENDNO"]) {
                    $contents2 .= "\n";
                    $gyo_no++;
                }
            }
            $contents2 .= $row["ATTENDNO"] .",";
            $contents2 .= $row["SEX"] .",";
            // 氏名漢字・ふりかな
            if ($this->field["OUTPUT"] == "1") {
                $contents2 .= $row["NAME"] .",";
                $contents2 .= $row["NAME_KANA"] ."\n";
            } else {
                $contents2 .= $row["NAME"] ."\n";
            }
            $gyo_no++;
            $header_data = true;
        }
        // 最後の年組を出力
        if ($grade_hr_class != "") {
            for ($i = 0; $i < $this->field["KENSUU"]; $i++) $contents .= $contents2 ."\n";
        }
        $result->free();
        Query::dbCheckIn($db);

        // ＣＳＶ出力
        if ($header_data) {
            $title = ($this->field["OUTPUT"] == "1") ? "名列（氏名漢字・ふりかな）" : "名列（氏名漢字のみ）";
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

}
?>
