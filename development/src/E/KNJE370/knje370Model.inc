<?php

require_once('for_php7.php');


class knje370Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $staffcd;        //職員コード
    var $selectdata;

    function knje370Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //職員コード
    }

    function init()
    {
        $this->field = array("YEAR"         =>  VARS::post("YEAR"),             //年度
                             "SEMESTER"     =>  VARS::post("SEMESTER"),         //学期
                             "GOUHI"        =>  VARS::post("GOUHI"),            //対象（合否・進路状況）コンボ
//                             "KUBUN"        =>  VARS::post("KUBUN"),            //種別（設置区分）コンボ
                             "OUT_DIV"      =>  VARS::post("OUT_DIV"),          //対象ラジオボタン 
                             "DATA_DIV"     =>  VARS::post("DATA_DIV"),         //データ区分
                             "KAIPAGE"      =>  VARS::post("KAIPAGE"),          //生徒毎に出力する
                             "GRADE_HR"     =>  VARS::post("GRADE_HR"),         //年組
                             "DBNAME"       =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->cmd         = VARS::request("cmd");
        $this->selectdata  = VARS::post("selectdata");
        $this->selecttypedata = VARS::post("selecttypedata");
        $this->selectdatatypecnt = VARS::post("SELECTDATA_TYPE_CNT");
    }

    function getDownloadModel() {
        if ($this->field["OUT_DIV"] == "1") {
            return $this->printSingaku();
        } else {
            return $this->printShushoku();
        }
    }

    function printSingaku() {
        //変数
        $senkouKind = "0";
        $gouhi = explode("-", $this->field["GOUHI"]); //gouhi[0]：名称コード１, gouhi[1]：名称コード２ (複合⇒[0]:CD1,[1]:"MIX",[2]:NAMESPARE)
        $kubunarry = array();
        $kubunarry = explode(",", $this->selecttypedata); //kubun[]
        $kubunName = "";
        if (get_count($kubunarry) == $this->selectdatatypecnt) {
            $kubunName = "全て";
            $kubunarry = array("E000-ALL");
        } else {
            $kubunName = "";
            asort($kubunarry);
        }
        $contents = "";

        //ヘッダ
        $header = array("SCHREGNO"          => "学籍番号",
                        "GRADE"             => "学年",
                        "HR_CLASS"          => "組",
                        "ATTENDNO"          => "出席番号",
                        "NAME"              => "氏名",
                        "NAME_KANA"         => "氏名かな",
                        "SEX"               => "性別",
                        "SCHOOL_GROUP_NAME" => "設置区分",
//                        "PREF_NAME"         => "所在地",
                        "STAT_CD"           => "大学コード",
                        "STAT_NAME"         => "学校名",
                        "FACULTYNAME"       => "学部",
                        "DEPARTMENTNAME"    => "学科",
                        "PROGRAM_NAME"      => "日程",
                        "FORM_NAME"         => "方式",
                        "HOWTOEXAM_NAME"    => "受験方式",
                        "DECISION_NAME"     => "合否",
                        "PLANSTAT_NAME"     => "進路状況",
                        "EXAMNO"            => "受験番号",
                        "CHKMARK"           => "匿名"
                        );
        //DB接続
        $db = Query::dbCheckOut();
        //タイトル
        $gouhiName = "全て";
        if ($gouhi[0] != "E000") {
            if ($gouhi[1] != "MIX") {
                $gouhiName = $db->getOne(knje370Query::getNameMst($model, $gouhi[0], $gouhi[1]));
            } else {
                $gouhiName = "";
                $sep = "";
                $query = knje370Query::getNameMst2($gouhi[0], $gouhi[2]);
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $gouhiName = $gouhiName.$sep.$row["LABEL"];
                    $sep = "・";
                }
                $result->free();
            }
        }
        $kubundelim = "";
        $kbncnt = 0;
        foreach ($kubunarry as $kbn) {
            if ($kbncnt >= 2) {
                continue;
            }
            $kubun = explode("-", $kbn);
            if ($kubun[0] != "E000") {
                $kubunName .= $kubundelim . $db->getOne(knje370Query::getNameMst($model, $kubun[0], $kubun[1]));
                $kubundelim = "_";
            }
            if (get_count($kubunarry) > 2 && (int)$kbncnt+1 >= 2) {
                $kubunName .= "_他";
            }
            $kbncnt++;
        }
        //SQL文発行
        $data = false;
//            $query = knje370Query::getPrintSingaku($this, $this->field["YEAR"], $this->field["SEMESTER"], $senkouKind, $gouhi[0], $gouhi[1], $kubun[0], $kubun[1], $this->selectdata);
        if ($gouhi[1] != "MIX") {
            $query = knje370Query::getPrintSingaku($this, $this->field["YEAR"], $this->field["SEMESTER"], $senkouKind, $gouhi[0], $gouhi[1], $kubunarry, $this->selectdata);
        } else {
            $query = knje370Query::getPrintSingaku($this, $this->field["YEAR"], $this->field["SEMESTER"], $senkouKind, $gouhi[0], $gouhi[2], $kubunarry, $this->selectdata, "MIX");
        }
        $result = $db->query($query);
        $befSchregNo = "";
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            if ($this->field["KAIPAGE"] == "1" && $befSchregNo != $row["SCHREGNO"] && $data) {
                $contents .= "\n";
                $contents .= "\n";
            }
            if (($this->field["KAIPAGE"] == "1" && $befSchregNo != $row["SCHREGNO"]) || !$data) {
                $contents .= implode($header, ",") ."\n";
            }

            //既卒生
            if (substr($row["GRADE"],0,2) == "ZZ") {
                //学年
                list ($dummyG, $year, $grade) = explode('-', $row["GRADE"]);
                $row["GRADE"]  = "";
                if ($year) $row["GRADE"] .= $year.'年度卒';
                if ($grade) $row["GRADE"] .= $grade;
                //組
                list ($dummyH, $hr_class) = explode('-', $row["HR_CLASS"]);
                $row["HR_CLASS"]  = "";
                if ($hr_class) $row["HR_CLASS"] .= $hr_class;
                //出席番号
                $row["ATTENDNO"] = "";
            }

            $sep = "";
            foreach ($header as $field => $name) {
                $contents .= $sep.$row[$field];
                $sep = ",";
            }
            $contents .= "\n";
            $data = true;
            $befSchregNo = $row["SCHREGNO"];
        }
        $result->free();
        //学期開始日取得
        $sdate = $db->getOne(knje370Query::getSdate($this));
        //DB切断
        Query::dbCheckIn($db);
        //ＣＳＶ出力
        if ($data) {
            $nendo = common::DateConv1(str_replace("-", "/", $sdate), 10) ."_";
            $title = $nendo ."進路状況一覧（進学：" .$kubunName ."）（" .$gouhiName ."）";
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }

    function printShushoku() {
        //変数
        $senkouKind = "1";
        $gouhi = explode("-", $this->field["GOUHI"]); //gouhi[0]：名称コード１, gouhi[1]：名称コード２ (複合⇒[0]:CD1,[1]:"MIX",[2]:NAMESPARE)
        $contents = "";
        //ヘッダ
        $header = array("SCHREGNO"          => "学籍番号",
                        "GRADE"             => "学年",
                        "HR_CLASS"          => "組",
                        "ATTENDNO"          => "出席番号",
                        "NAME"              => "氏名",
                        "NAME_KANA"         => "氏名かな",
                        "SEX"               => "性別",
                        "STAT_NAME"         => "会社名",
                        "INDUSTRY_LNAME"    => "産業種別",
                        "PREF_NAME"         => "就業場所",
                        "HOWTOEXAM_NAME"    => "応募方法",
                        "DECISION_NAME"     => "合否",
                        "PLANSTAT_NAME"     => "進路状況",
                        "REMARK"            => "備考"
                        );
        //DB接続
        $db = Query::dbCheckOut();
        //タイトル
        $gouhiName = "全て";
        $kubunName = "就職";
        if ($gouhi[0] != "E000") {
            if ($gouhi[1] != "MIX") {
                $gouhiName = $db->getOne(knje370Query::getNameMst($model, $gouhi[0], $gouhi[1]));
            } else {
                $gouhiName = "";
                $sep = "";
                $query = knje370Query::getNameMst2($gouhi[0], $gouhi[2]);
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $gouhiName = $gouhiName.$sep.$row["LABEL"];
                    $sep = "・";
                }
                $result->free();
            }
        }
        
        //SQL文発行
        $data = false;
        if ($gouhi[1] != "MIX") {
            $query = knje370Query::getPrintShushoku($this, $this->field["YEAR"], $this->field["SEMESTER"], $senkouKind, $gouhi[0], $gouhi[1], $this->selectdata);
        } else {
            $query = knje370Query::getPrintShushoku($this, $this->field["YEAR"], $this->field["SEMESTER"], $senkouKind, $gouhi[0], $gouhi[2], $this->selectdata, "MIX");
        }
        $result = $db->query($query);
        $befSchregNo = "";
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            if ($this->field["KAIPAGE"] == "1" && $befSchregNo != $row["SCHREGNO"] && $data) {
                $contents .= "\n";
                $contents .= "\n";
            }
            if (($this->field["KAIPAGE"] == "1" && $befSchregNo != $row["SCHREGNO"]) || !$data) {
                $contents .= implode($header, ",") ."\n";
            }

            //既卒生
            if (substr($row["GRADE"],0,2) == "ZZ") {
                //学年
                list ($dummyG, $year, $grade) = explode('-', $row["GRADE"]);
                $row["GRADE"]  = "";
                if ($year) $row["GRADE"] .= $year.'年度卒';
                if ($grade) $row["GRADE"] .= $grade;
                //組
                list ($dummyH, $hr_class) = explode('-', $row["HR_CLASS"]);
                $row["HR_CLASS"]  = "";
                if ($hr_class) $row["HR_CLASS"] .= $hr_class;
                //出席番号
                $row["ATTENDNO"] = "";
            }

            foreach ($header as $field => $name) {
                if ($field != "REMARK") {
                    $contents .= $row[$field] .",";
                }
            }
            $contents .= "\n";
            $data = true;
            $befSchregNo = $row["SCHREGNO"];
        }
        $result->free();
        //学期開始日取得
        $sdate = $db->getOne(knje370Query::getSdate($this));
        //DB切断
        Query::dbCheckIn($db);
        //ＣＳＶ出力
        if ($data) {
            $nendo = common::DateConv1(str_replace("-", "/", $sdate), 10) ."_";
            $title = $nendo ."進路状況一覧（就職：" .$gouhiName ."）";
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
?>
