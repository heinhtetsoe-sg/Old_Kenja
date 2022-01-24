<?php

require_once('for_php7.php');

class knja224Model extends Model
{
    public $field      = array();
    public $cmd;
    public $control;        //コントロールマスタ
    public $staffcd;        //職員コード
    public $selectdata;
    public $schoolName;
    public $schoolCode;
    public $isTuusin;

    public function knja224Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;       //職員コード
    }

    public function init()
    {
        $this->field = array("YEAR"              =>  VARS::post("YEAR"),             //年度
                             "OUTPUT"            =>  VARS::post("OUTPUT"),           //名票種別選択
                             "KARA"              =>  VARS::post("KARA"),             //空行を詰めて印字
                             "PRINT_SCHREGNO"    =>  VARS::post("PRINT_SCHREGNO"),   //学籍番号を表記する
                             "KYOUMU"            =>  VARS::post("KYOUMU"),           //教務種類
                             "KENSUU"            =>  VARS::post("KENSUU"),           //出力件数
                             "WAKU_NASI"         =>  VARS::post("WAKU_NASI"),        //枠無し
                             "NAME_NASI"         =>  VARS::post("NAME_NASI"),        //名前無し
                             "GAKKI"             =>  VARS::post("GAKKI"),            //学期
                             "DBNAME"            =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->selectdata  = VARS::post("selectdata");

        $this->cmd         = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //学校名取得
        $query = knja224Query::getSchoolName();
        $db = Query::dbCheckOut();
        $rowZ010 = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $this->schoolName = $rowZ010["NAME1"];
        $this->schoolCode = $rowZ010["NAME2"];
        Query::dbCheckIn($db);

        $query = knja224Query::getTuusinflg();
        $db = Query::dbCheckOut();
        $rowZ001 = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $this->isTuusin = $rowZ001["NAMESPARE3"] == null ? "" : $rowZ001["NAMESPARE3"];
        Query::dbCheckIn($db);
    }


    // ＣＳＶ出力
    public function getDownloadModel()
    {
        $header1 = array("HR_NAMEABBV"      => "",
                         "BLANK"            => "",
                         "STAFFNAME"        => "");

        switch ($this->field["OUTPUT"]) {
            case "1":
                $header2 = array("ATTENDNO"         => "出席番号",
                                 "SEX"              => "性別",
                                 "NAME"             => "氏名",
                                 "NAME_KANA"        => "かな");
                break;
            case "2":
                $header2 = array("ATTENDNO"         => "出席番号",
                                 "SEX"              => "性別",
                                 "NAME"             => "氏名");
                break;
            case "3":
                $header2 = array("ATTENDNO"         => "出席番号",
                                 "SEX"              => "性別",
                                 "NAME"             => "氏名",
                                 "FINSCHOOL"        => "出身学校");
                break;
            case "4":
                $header2 = array("ATTENDNO"         => "出席番号",
                                 "SEX"              => "性別",
                                 "NAME"             => "氏名");
                break;
            case "5":
            case "musashi":
            case "chiben":
                if ($this->field["PRINT_SCHREGNO"] == "1" && $this->schoolName != 'tosajoshi' && $this->schoolName != 'osakatoin') {
                    $header2 = array("ATTENDNO"         => "出席番号",
                                     "SEX"              => "性別",
                                     "SCHREGNO"         => "学籍番号",
                                     "NAME"             => "氏名");
                } else {
                    $header2 = array("ATTENDNO"         => "出席番号",
                                     "SEX"              => "性別",
                                     "NAME"             => "氏名");
                }
                break;
            case "6":
                if ($this->field["KYOUMU"] == "1") {
                    $header2 = array("HR_CLASS"         => "組",
                                     "ATTENDNO"         => "出席番号",
                                     "NAME"             => "氏名");
                } else {
                    $header2 = array("ATTENDNO"         => "出席番号",
                                     "NAME"             => "氏名");
                }
                break;
            case "7":
                $header2 = array("ATTENDNO"         => "出席番号",
                                 "SEX"              => "性別",
                                 "NAME"             => "氏名",
                                 "NAME_KANA"        => "自由");
                break;
            case "8":
                $header2 = array("ATTENDNO"         => "出席番号",
                                 "SEX"              => "性別",
                                 "NAME"             => "氏名",
                                 "GRD_DIV"          => "異動区分");
                break;
        }

        $db = Query::dbCheckOut();

        $gyo_no = 1;
        $grade_hr_class = "";
        $header_data = false;
        $contents = $contents2 = "";

        $selectdata = array();
        $selectdata = explode(",", $this->selectdata);

        //職員情報取得
        $staffInfo = $db->getRow(knja224Query::getStaffInfo(), DB_FETCHMODE_ASSOC);
        $jpOrEngFlg = $staffInfo["FIELD5"] ? true : false;
        
        for ($grd_cls = 0; $grd_cls < get_count($selectdata); $grd_cls++) {
            $query = knja224Query::selectCsvQuery($selectdata[$grd_cls], $this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                // データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                $row = str_replace(",", ".", $row);

                // 年組コードのブレイク時
                if ($grade_hr_class != $row["GRADE_HR_CLASS"]) {
                    if ($grade_hr_class != "") {
                        for ($i = 0; $i < $this->field["KENSUU"]; $i++) {
                            $contents .= $contents2 ."\n";
                        }
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
                if ($this->field["OUTPUT"] == "6" && $this->field["KYOUMU"] == "1") {
                    $contents2 .= $row["HR_CLASS"] .",";
                }
                $contents2 .= $row["ATTENDNO"] .",";
                if ($this->field["OUTPUT"] != "6") {
                    $contents2 .= $row["SEX"] .",";
                }
                if ($this->field["OUTPUT"] == "5" && $this->field["PRINT_SCHREGNO"] == "1" && $this->schoolName != 'tosajoshi' && $this->schoolName != 'osakatoin') {
                    $contents2 .= $row["SCHREGNO"] .",";
                }

                //日本語・英語切替
                if ($jpOrEngFlg) {
                    $row["NAME_KANA"] = $row["NAME_ENG"];
                }
                switch ($this->field["OUTPUT"]) {
                    case "1":
                        $contents2 .= $row["NAME"] .",";
                        $contents2 .= $row["NAME_KANA"] ."\n";
                        break;
                    case "2":
                    case "6":
                        $contents2 .= $row["NAME"] ."\n";
                        break;
                    case "3":
                        $contents2 .= $row["NAME"] .",";
                        $contents2 .= $row["FINSCHOOL_NAME"] ."\n";
                        break;
                    case "4":
                        $contents2 .= $row["NAME"] ."\n";
                        break;
                    case "5":
                    case "musashi":
                    case "chiben":
                        $contents2 .= $row["NAME"] ."\n";
                        break;
                    case "7":
                        //名前だけ出力し、かなの部分は空で出力
                        $contents2 .= $row["NAME"] .",,"."\n";
                        break;
                    case "8":
                        // 2:休学 の場合は、同一年度の場合のみ出力する
                        if ($row["TRANSFERCD"] == 2) {
                            if (($row["TRANSFER_SDATE"] != null) && (formatNendo($row["TRANSFER_SDATE"]) == formatNendo(CTRL_DATE))) {
                                $contents2 .= $row["NAME"] .",";
                                $suffix = $row["GRD_NAME"] != null ? "（".$row["GRD_NAME"]."）" : "";
                                $contents2 .= $suffix ."\n";
                            } else {
                                $contents2 .= ",\n";
                            }
                        } else {
                            $contents2 .= $row["NAME"] .",";
                            $suffix = "";
                            if ($row["GRD_NAME"] != null) {
                                $suffix = "（".$row["GRD_NAME"]."）";
                            } elseif ($row["TRANSFER_NAME"] != null) {
                                $suffix = "（".$row["TRANSFER_NAME"]."）";
                            }
                            $contents2 .= $suffix ."\n";
                        }
                        break;
                }

                $gyo_no++;
                $header_data = true;
            }
        }
        // 最後の年組を出力
        if ($grade_hr_class != "") {
            for ($i = 0; $i < $this->field["KENSUU"]; $i++) {
                $contents .= $contents2 ."\n";
            }
        }
        $result->free();
        Query::dbCheckIn($db);

        // ＣＳＶ出力
        if ($header_data) {
            switch ($this->field["OUTPUT"]) {
                case "1":
                    $title = "名票（氏名漢字・ふりかな）";
                    break;
                case "2":
                    $title = "名票（氏名漢字のみ）";
                    break;
                case "3":
                    $title = "名票（氏名漢字・出身校）";
                    break;
                case "4":
                    $title = "名票（Ａ３ヨコ）";
                    break;
                case "5":
                    $title = "名票（氏名漢字・サイズ指定）";
                    break;
                case "6":
                    $title = "名票（組）";
                    break;
                case "musashi":
                case "chiben":
                    $title = "名票";
                    break;
                case "7":
                    $title = "名票（氏名漢字・自由）";
                    break;
                case "8":
                    $title = "名票（氏名漢字・異動区分）";
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

function formatNendo($dateStr)
{
    $year = substr($dateStr, 0, 4);
    $month = substr($dateStr, 5, 7);

    // 1月 ～ 3月は －１年した値を年度とする
    if (("1" == $month) || ("2" == $month) || ("3" == $month)) {
        $year = $year - 1;
    }

    return $year;
}
