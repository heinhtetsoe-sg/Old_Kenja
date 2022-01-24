<?php

require_once('for_php7.php');

class knjahamaModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $staffcd;        //職員コード
    var $selectdata;
    var $schoolName;
    var $schoolCode;

    function knjahamaModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;       //職員コード
    }

    function init() {
        $this->field = array("YEAR"              =>  VARS::post("YEAR"),             //年度
                             "OUTPUT"            =>  VARS::post("OUTPUT"),           //名票種別選択
                             "KARA"              =>  VARS::post("KARA"),             //空行を詰めて印字
                             "KENSUU"            =>  VARS::post("KENSUU"),           //出力件数
                             "WAKU_NASI"         =>  VARS::post("WAKU_NASI"),        //枠無し
                             "NAME_NASI"         =>  VARS::post("NAME_NASI"),        //名前無し
                             "GAKKI"             =>  VARS::post("GAKKI"),            //学期
                             "DBNAME"            =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->selectdata  = VARS::post("selectdata");

        $this->cmd         = VARS::request("cmd");

        //学校名取得
        $query = knjahamaQuery::getSchoolName();
        $db = Query::dbCheckOut();
        $rowZ010 = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $this->schoolName = $rowZ010["NAME1"];
        $this->schoolCode = $rowZ010["NAME2"];
        Query::dbCheckIn($db);
    }


    // ＣＳＶ出力
    function getDownloadModel() {
        $header1 = array("HR_NAMEABBV"      => "",
                         "BLANK"            => "",
                         "STAFFNAME"        => "");

        switch ($this->field["OUTPUT"]) {
            case "1";
                $header2 = array("ATTENDNO"         => "出席番号",
                                 "SEX"              => "性別",
                                 "NAME"             => "氏名",
                                 "NAME_KANA"        => "かな");
                break;
            case "2";
                $header2 = array("ATTENDNO"         => "出席番号",
                                 "SEX"              => "性別",
                                 "NAME"             => "氏名");
                break;
            case "3";
                $header2 = array("ATTENDNO"         => "出席番号",
                                 "SEX"              => "性別",
                                 "NAME"             => "氏名",
                                 "FINSCHOOL"        => "出身学校");
                break;
            case "4";
                $header2 = array("ATTENDNO"         => "出席番号",
                                 "SEX"              => "性別",
                                 "NAME"             => "氏名");
                break;
            case "5";
            case "musashi";
                $header2 = array("ATTENDNO"         => "出席番号",
                                 "SEX"              => "性別",
                                 "NAME"             => "氏名");
                break;
        }

        $db = Query::dbCheckOut();

        $gyo_no = 1;
        $grade_hr_class = "";
        $header_data = false;
        $contents = $contents2 = "";

        $selectdata = array();
        $selectdata = explode(",", $this->selectdata);

        for($grd_cls = 0; $grd_cls < get_count($selectdata); $grd_cls++){

            $query = knjahamaQuery::selectCsvQuery($selectdata[$grd_cls], $this);
            $result = $db->query($query);
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
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
                if ($this->field["KARA"] != "1") {
                    while ($gyo_no < $row["ATTENDNO"]) {
                        $contents2 .= "\n";
                        $gyo_no++;
                    }
                }
                $contents2 .= $row["ATTENDNO"] .",";
                $contents2 .= $row["SEX"] .",";

                switch ($this->field["OUTPUT"]) {
                    case "1";
                        $contents2 .= $row["NAME"] .",";
                        $contents2 .= $row["NAME_KANA"] ."\n";
                        break;
                    case "2";
                        $contents2 .= $row["NAME"] ."\n";
                        break;
                    case "3";
                        $contents2 .= $row["NAME"] .",";
                        $contents2 .= $row["FINSCHOOL_NAME"] ."\n";
                        break;
                    case "4";
                        $contents2 .= $row["NAME"] ."\n";
                        break;
                    case "5";
                    case "musashi";
                        $contents2 .= $row["NAME"] ."\n";
                        break;
                }

                $gyo_no++;
                $header_data = true;
            }
        }
        // 最後の年組を出力
        if ($grade_hr_class != "") {
            for ($i = 0; $i < $this->field["KENSUU"]; $i++) $contents .= $contents2 ."\n";
        }
        $result->free();
        Query::dbCheckIn($db);

        // ＣＳＶ出力
        if($header_data) {
            switch ($this->field["OUTPUT"]) {
                case "1";
                    $title = "名票（氏名漢字・ふりかな）";
                    break;
                case "2";
                    $title = "名票（氏名漢字のみ）";
                    break;
                case "3";
                    $title = "名票（氏名漢字・出身校）";
                    break;
                case "4";
                    $title = "名票（Ａ３ヨコ）";
                    break;
                case "5";
                    $title = "名票（氏名漢字・サイズ指定）";
                    break;
                case "musashi";
                    $title = "名票";
                    break;
            }

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
