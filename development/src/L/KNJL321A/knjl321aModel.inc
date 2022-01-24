<?php
class knjl321aModel extends Model
{
    public $field = array();
    public $cmd;
    public $ObjYear;

    public function knjl321aModel()
    {
    }

    public function init()
    {
        $this->ObjYear = CTRL_YEAR+1;
        $this->examType = 1;

        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),        //受験校種
                             "TESTDIV"          =>  VARS::post("TESTDIV"),             //試験回
                             "SHDIV"            =>  VARS::post("SHDIV"),               //専併区分
                             "EXAMCOURSE"       =>  VARS::post("EXAMCOURSE"),          //志望コース
                             "SORT"             =>  VARS::post("SORT"),                //出力順
                             "APPINFO_FLG"             =>  VARS::post("APPINFO_FLG"),  //志願者情報出力チェック
                     );

        $this->cmd = VARS::request("cmd");
    }

    //CVSファイル
    //サーバ ⇒ クライアント
    public function getDownloadCsvModel()
    {
        $db = Query::dbCheckOut();

        //受験校種(J,H)
        $query = knjl321aQuery::getSchoolKind($this);
        $this->schoolKind = $db->getOne($query);

        //受験科目取得
        $this->subClassArr = array();
        $query  = knjl321aQuery::getNameMst($this->ObjYear, "L009");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["NAMECD2"] == "6") {
                continue;
            } //面接は除く
            $this->subClassArr[$row["NAMECD2"]] = ($this->field["APPLICANTDIV"] == "1") ? $row["NAME1"] : $row["NAME2"];
        }

        //プレテスト、プレテスト以外をそれぞれ取得
        $this->preTestArr = array();
        $this->notPreTestArr = array();
        $query  = knjl321aQuery::getTestdivMst($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["PRETEST_FLG"] != "1") {
                $this->notPreTestArr[$row["TESTDIV"]] = $row["TESTDIV_ABBV"];
            } else {
                $this->preTestArr[$row["TESTDIV"]] = $row["TESTDIV_NAME"];
            }
        }

        //ヘッダー
        $header  = array();
        $header2 = array();
        if ($this->field["APPLICANTDIV"] == "1") {
            $header["RECEPTNO"] = "受験番号";
            $header["SHDIV_NAME"] = "専併";
            $header["COURSE_NAME"] = "コース";
            $header["NAME"] = "氏名";
            $header["SEX_ABBV"] = "性別";
            $header["FS_CD"] = "出身学校コード";
            $header["FINSCHOOL_NAME_ABBV"] = "出身学校";
            foreach ($this->subClassArr as $subCd => $subName) {
                $header["SCORE".$subCd] = $subName;
            }
            $header["MINASHI"] = "英検";
            $header["TOTAL4"] = "４科合計";
            $header["TOTAL3"] = "３科合計";
            $header["TOTAL2"] = "２科合計";
            $header["TOTAL1"] = "判定合計";
            $header["SCORE6"] = "面接点";
            foreach ($this->preTestArr as $preCd => $preName) {
                $header["PRE_TOTAL".$preCd] = $preName;
            }
        } else {
            $header["COURSE_RANK"] = "コース別順位";
            $header["ALL_RANK"] = "全体順位";
            $header["RECEPTNO"] = "受験番号";
            $header["SHDIV_NAME"] = "専併";
            $header["COURSE_NAME"] = "コース";
            $header["NAME"] = "氏名";
            $header["SEX_ABBV"] = "性別";
            $header["FS_CD"] = "出身学校コード";
            $header["FINSCHOOL_NAME_ABBV"] = "出身学校";
            foreach ($this->subClassArr as $subCd => $subName) {
                $header["SCORE".$subCd] = $subName;
            }
            $header["MINASHI"] = "英検";
            $header["TOTAL1"] = "５科合計";
            $header["SCORE6"] = "面接点";
        }
        $header["HONORDIV0"] = "確定特待";
        $header["HONORDIV1"] = "事前特待";
        $header["HONORDIV2"] = "特待申請";
        $header["HONORDIV3"] = "資格活用";
        $header["JIZEN_SENGAN"] = "事前専願";
        $header["DESIREDIV"] = "事前判定コース";
        $header["ENT_COURSE"] = "合格コース";
        $header["SENGAN_ENT"] = "専願合格";
        $header["HONOR_REMARK"] = "特待備考";
        foreach ($this->notPreTestArr as $testDiv => $testDivAbbv) {
            $header["TESTDIV_RESULT".$testDiv] = $testDivAbbv;
        }
        if ($this->field["APPINFO_FLG"] == "1") {
            $header2["EXAMNO"]           = "志願者SEQ";
            $header2["NAME_KANA"]        = "氏名かな";
            $header2["BIRTHDAY"]         = "生年月日";
            $header2["TELNO"]            = "電話番号";
            $header2["ZIPCD"]            = "郵便番号";
            $header2["ADDRESS1"]         = "住所";
            $header2["ADDRESS2"]         = "方書";
            $header2["GNAME"]            = "保護者氏名";
            $header2["GKANA"]            = "保護者氏名かな";
            $header2["EMERGENCYTELNO"]   = "緊急連絡先";
            $header2["FS_YM"]            = "卒業年月日";
            $header2["FS_GRDDIV_NAME"]   = "種類（見込、卒業）";
        }

        $contents = ""; //クリア
        $data = false; //出力フラグ

        //ヘッダ
        $contents .= implode($header, ",");
        if ($this->field["APPINFO_FLG"] == "1") {
            $contents .= ",".implode($header2, ",");
        }
        $contents .= "\n";

        //データ
        $query = knjl321aQuery::getCsvQuery($this);

        //他合格コース取得
        $receptPassCourses = array();
        $query2 = knjl321aQuery::getCsvPassCourseQuery($this);

        $result = $db->query($query2);
        while ($receptRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $senganPassDiv      = $receptRow["SENGAN_PASS_DIV"];
            $heiganPassDiv      = $receptRow["HEIGAN_PASS_DIV"];
            $senganCourseName   = ($senganPassDiv != "") ? $receptRow["SENGAN"] : "";
            $heiganCourseName   = ($heiganPassDiv != "") ? $receptRow["HEIGAN"] : "";
            $receptPassCourses[$receptRow["EXAMNO"]][$receptRow["TESTDIV"]] = array("SENGAN_PASS_NAME" => $senganCourseName,
                                                                                      "HEIGAN_PASS_NAME" => $heiganCourseName);
        }

        //SQL文発行
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace(array("\r\n", "\r", "\n"), "↓", str_replace(",", ".", $row));

            //他入試区分の合格コースをセット
            foreach ($this->notPreTestArr as $testDiv => $testDivAbbv) {
                $testDivPassCourseName = "";
                if (isset($receptPassCourses[$row["EXAMNO"]]) && $receptPassCourses[$row["EXAMNO"]][$testDiv]) {
                    //志願者の専併区分に対応した合格コースを出力
                    $keyStr = ($row["SHDIV"] == "1") ? "SENGAN_PASS_NAME" : "HEIGAN_PASS_NAME";
                    $testDivPassCourseName = $receptPassCourses[$row["EXAMNO"]][$testDiv][$keyStr];
                }
                $row["TESTDIV_RESULT".$testDiv] = $testDivPassCourseName;
            }

            //日付変換
            $row["BIRTHDAY"] = str_replace("-", "/", $row["BIRTHDAY"]);

            //卒業年月整形
            $fsY = ($row["FS_GRDYEAR"] != "") ? $row["FS_GRDYEAR"] : $this->ObjYear;
            $fsM = ($row["FS_M"] != "") ? $row["FS_M"] : "03";
            $row["FS_YM"] = $fsY."/".$fsM;

            //ヘッダ順に値をセット
            $sep = "";
            foreach ($header as $field => $label) {
                $contents .= $sep.$row[$field];
                $sep = ",";
            }
            foreach ($header2 as $field => $label) {
                $contents .= $sep.$row[$field];
                $sep = ",";
            }
            $contents .= "\n";

            $data = true;
        }
        $result->free();

        //受験校種
        $appdivName = $db->getOne(knjl321aQuery::getNameMst($this->ObjYear, "L003", $this->field["APPLICANTDIV"]));
        //試験回
        $testdivName = $db->getOne(knjl321aQuery::getTestdivMst($this, $this->field["TESTDIV"]));
        //専併区分
        if (strlen($this->field["SHDIV"]) && $this->field["SHDIV"] != "ALL") {
            $shdivName = $db->getOne(knjl321aQuery::getNameMst($this->ObjYear, "L006", $this->field["SHDIV"]));
        } else {
            $shdivName = ($this->field["SHDIV"] == "ALL") ? "専願併願" : "";
        }
        //志望コース
        if (strlen($this->field["EXAMCOURSE"]) && $this->field["EXAMCOURSE"] != "ALL") {
            $courseName = $db->getOne(knjl321aQuery::getNameMst($this->ObjYear, ($this->field["APPLICANTDIV"] == "1") ? "LJ58" : "LH58", $this->field["EXAMCOURSE"]));
        } else {
            $courseName = ($this->field["EXAMCOURSE"] == "ALL") ? "コース全て" : "";
        }
        //出力順
        $sortName = ($this->field["SORT"] == "1") ? "成績順" : "受験番号順";

        Query::dbCheckIn($db);

        //CSV出力
        if ($data) {
            $fname  = $this->ObjYear."年度";
            $fname .= "_判定会議資料";
            $fname .= "_".$appdivName;
            $fname .= "_".$testdivName;
            if (strlen($shdivName)) {
                $fname .= "_".$shdivName;
            }
            if (strlen($courseName)) {
                $fname .= "_".$courseName;
            }
            $fname .= "_".$sortName;
            $filename = sprintf("%s.csv", $fname);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
