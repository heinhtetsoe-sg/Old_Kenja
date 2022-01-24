<?php

require_once('for_php7.php');


class knjl351w_1Model extends Model {
    var $field      = array();
    var $data       = array();
    var $cmd;
    var $file;
    var $control;

    /**
     * メンバ変数の初期化
     */
    function knjl351w_1Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("OUTPUT_DIV"       =>  VARS::post("OUTPUT_DIV"),
                             "CSV_DIV"          =>  VARS::post("CSV_DIV")
                             );
        $this->entexamYear = CTRL_YEAR + 1;

        $this->cmd = VARS::request("cmd");

        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    function &getMainModel() {
    }

    //ＣＳＶ出力
    function getDownloadModel()
    {
        //DB接続
        $db = Query::dbCheckOut2();
        $dataAri = false;

        //入試制度
        $query = knjl351w_1Query::get_name_cd($this->entexamYear, "L003");
        $this->keyArray = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->keyArray[$row["VALUE"]]["APPLICANTNAME"] = $row["LABEL"];
        }
        $result->free();

        //入試区分
        foreach ($this->keyArray as $applicantDiv => $val) {
            $query = knjl351w_1Query::get_name_cdAft($this, "L004", "TESTDIV", $applicantDiv);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $this->keyArray[$applicantDiv]["TESTDIV"][$row["VALUE"]]["TESTNAME"] = $row["LABEL"];
            }
            $result->free();
        }

        //累計区分
        foreach ($this->keyArray as $applicantDiv => $applicantVal) {
            foreach ($applicantVal["TESTDIV"] as $testDiv => $testVal) {
                $query = knjl351w_1Query::get_name_cdAft($this, "L040", "RUIKEI_DIV", $applicantDiv, $testDiv, "0");
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $this->keyArray[$applicantDiv]["TESTDIV"][$testDiv]["RUIKEI_DIV"][$row["VALUE"]]["RUIKEINAME"] = $row["LABEL"];
                }
                $result->free();
            }
        }

        //タイトル
        $this->title = array();
        $nyuugakuFlg = false;
        $contentsTitle = "";
        $contentsTitle = ",,,,,";
        $this->selectArray = array();
        $this->selectArray2 = array();
        $bosyuFlg = false;
        foreach ($this->keyArray as $applicantDiv => $applicantVal) {
            foreach ($applicantVal["TESTDIV"] as $testDiv => $testVal) {
                $bosyuFlg = false;
                $contentsTitle .= $testVal["TESTNAME"];
                foreach ($testVal["RUIKEI_DIV"] as $ruikeiDiv => $ruikeiVal) {
                    $query = knjl351w_1Query::getTitleQuery($this, $applicantDiv, $testDiv, "0", $ruikeiDiv);
                    $result = $db->query($query);
                    while ($titleRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                        if ($bosyuFlg && $titleRow["LARGE_DIV"] == "99") {
                            continue;
                        }
                        $contentsTitle .= ",";
                        $this->title[$applicantDiv.$testDiv.$ruikeiDiv][$titleRow["LARGE_DIV"]] = $titleRow["LARGE_NAME"];
                        if (get_count($this->selectArray) < 50) {
                            $this->selectArray[] = $applicantDiv.$testDiv.$ruikeiDiv."0".$titleRow["LARGE_DIV"];
                        } else {
                            $this->selectArray2[] = $applicantDiv.$testDiv.$ruikeiDiv."0".$titleRow["LARGE_DIV"];
                        }
                        $nyuugakuFlg = true;
                        $bosyuFlg = true;
                    }
                    $result->free();
                }
            }
        }

        //タイトル追検査
        $this->selectTsuiKensaArray = array();
        $this->selectTsuiKensaArray2 = array();
        foreach ($this->keyArray as $applicantDiv => $applicantVal) {
            $query = knjl351w_1Query::getTitleTsuiKensaQuery($this, $applicantDiv, "1");
            $result = $db->query($query);
            $contentsTitle .= "追検査";
            while ($titleRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $contentsTitle .= ",";
                $this->title[$applicantDiv."Z".$titleRow["RUIKEI_DIV"]][$titleRow["LARGE_DIV"]] = $titleRow["LARGE_NAME"];
                if (get_count($this->selectTsuiKensaArray) < 50) {
                    $this->selectTsuiKensaArray[] = $applicantDiv.$titleRow["RUIKEI_DIV"].$titleRow["TESTDIV2"].$titleRow["LARGE_DIV"];
                } else {
                    $this->selectTsuiKensaArray2[] = $applicantDiv.$titleRow["RUIKEI_DIV"].$titleRow["TESTDIV2"].$titleRow["LARGE_DIV"];
                }
            }
            $result->free();
        }

        $contentsTitle2 = "";
        $contentsTitle2 = "学校CD,課程学科コースCD,高等学校名,学科・コース名,入学定員";
        foreach ($this->title as $key => $val) {
            foreach ($val as $large => $largename) {
                $contentsTitle2 .= ",".$largename;
            }
        }

        //定員
        $query = knjl351w_1Query::getCapacity($this);
        $this->capacityArray = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->capacityArray[$row["EDBOARD_SCHOOLCD"].$row["COURSECD"].$row["MAJORCD"].$row["EXAMCOURSECD"]] = $row;
        }
        $result->free();

        //CSV1追検査
        $query = knjl351w_1Query::getCsvQuery1TsuiKensa($this, $this->selectTsuiKensaArray, 0);
        $result = $db->query($query);
        $this->tsuikensaArray = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->tsuikensaArray[$row["COURSECD"].$row["MAJORCD"].$row["COURSECODE"].$row["EDBOARD_SCHOOLCD"]] = $row;
        }
        $result->free();
        if (get_count($this->selectTsuiKensaArray2) > 0) {
            $query = knjl351w_1Query::getCsvQuery1TsuiKensa($this, $this->selectTsuiKensaArray2, 50);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                foreach ($row as $key => $val) {
                    $this->tsuikensaArray[$row["COURSECD"].$row["MAJORCD"].$row["COURSECODE"].$row["EDBOARD_SCHOOLCD"]][$key] = $val;
                }
            }
            $result->free();
        }

        $contents = "";
        //CSV1
        $query = knjl351w_1Query::getCsvQuery1($this, $this->selectArray, 0);
        $result = $db->query($query);
        $this->kensaArray = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->kensaArray[$row["COURSECD"].$row["MAJORCD"].$row["COURSECODE"].$row["EDBOARD_SCHOOLCD"]] = $row;
        }
        $result->free();

        if (get_count($this->selectArray2) > 0) {
            $query = knjl351w_1Query::getCsvQuery1($this, $this->selectArray2, 50);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                foreach ($row as $key => $val) {
                    $this->kensaArray[$row["COURSECD"].$row["MAJORCD"].$row["COURSECODE"].$row["EDBOARD_SCHOOLCD"]][$key] = $val;
                }
            }
            $result->free();
        }

        $koumokuOutPut = false;
        $befCoureCd = "";
        $befSchoolCd = "";
        foreach ($this->kensaArray as $fkey => $fval) {
            if (!$koumokuOutPut) {
                $contents .= $contentsTitle."\n";
                $contents .= $contentsTitle2."\n";
                $koumokuOutPut = true;
            }
            $contents .= $fval["EDBOARD_SCHOOLCD"].",";
            $contents .= $fval["COURSECD"].$fval["MAJORCD"].$fval["COURSECODE"].",";
            if ($befSchoolCd != $fval["EDBOARD_SCHOOLCD"]) {
                $contents .= $fval["EDBOARD_SCHOOLNAME"].",";
            } else {
                $contents .= ",";
            }
            $contents .= $fval["MAJORNAME"].$fval["COURSECODENAME"].",";
            $setCapa = $this->capacityArray[$fval["EDBOARD_SCHOOLCD"].$fval["COURSECD"].$fval["MAJORCD"].$fval["COURSECODE"]]["CAPACITY"];
            $contents .= $setCapa;
            foreach ($this->selectArray as $key => $val) {
                $contents .= ",".$fval["COUNT{$key}"];
            }
            foreach ($this->selectArray2 as $key => $val) {
                $kkey = $key + 50;
                $contents .= ",".$fval["COUNT{$kkey}"];
            }
            foreach ($this->selectTsuiKensaArray as $key => $val) {
                $contents .= ",".$this->tsuikensaArray[$fval["COURSECD"].$fval["MAJORCD"].$fval["COURSECODE"].$fval["EDBOARD_SCHOOLCD"]]["COUNT{$key}"];
            }
            foreach ($this->selectTsuiKensaArray2 as $key => $val) {
                $kkey = $key + 50;
                $contents .= ",".$this->tsuikensaArray[$fval["COURSECD"].$fval["MAJORCD"].$fval["COURSECODE"].$fval["EDBOARD_SCHOOLCD"]]["COUNT{$kkey}"];
            }
            $dataAri = true;
            $befCoureCd = $fval["COURSECD"];
            $befSchoolCd = $fval["EDBOARD_SCHOOLCD"];
            $contents .= "\n";
        }

        //DB切断
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($dataAri) {
            $couse = $this->entexamYear."年度".$setTestName."集計結果";
            $filename = sprintf("%s.csv",$couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
?>
