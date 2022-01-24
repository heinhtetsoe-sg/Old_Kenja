<?php

require_once('for_php7.php');


class knjl330wModel extends Model {
    var $field      = array();
    var $data       = array();
    var $cmd;
    var $file;
    var $control;

    /**
     * メンバ変数の初期化
     */
    function knjl330wModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),
                             "TESTDIV"          =>  VARS::post("TESTDIV"),
                             "TESTDIV2"         =>  VARS::post("TESTDIV2"),
                             );
        $this->entexamYear = CTRL_YEAR + 1;

        $this->cmd  = VARS::request("cmd");
        $this->selectData = explode(",", VARS::post("selectdata"));
        $this->field["TESTDIV2"] = $this->field["TESTDIV2"] ? $this->field["TESTDIV2"] : "0";

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
        $db = Query::dbCheckOut();

        //テスト名
        $query = knjl330wQuery::getTestDivName($this);
        $setTestName = $db->getOne($query);

        //累計区分取得
        //志願者数、合格内定者数…等
        $query = knjl330wQuery::get_name_cdAft($this, "L040", "RUIKEI_DIV");
        $result = $db->query($query);
        $this->ruikeiArray = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->ruikeiArray[] = $row;
        }
        $result->free();

        //累計区分毎に出力する項目を取得する。
        //志願者数
        //志願者数、覚書志願者内数、海外帰国生徒等内数…等
        $this->titleArray = array();
        foreach ($this->ruikeiArray as $key => $val) {
            $query = knjl330wQuery::getTitleQuery($this, $val["VALUE"]);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $this->titleArray[$key][] = $row;
            }
            $result->free();
        }

        //出力データをあらかじめ取得
        //学校＋累計区分＋課程のコースも保持
        $query = knjl330wQuery::selectMainQuery($this);
        $result = $db->query($query);
        $this->allData = array();
        $this->schoolRuikeiCourse = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $dataKey = $row["EDBOARD_SCHOOLCD"].$row["RUIKEI_DIV"].$row["COURSECD"].$row["MAJORCD"].$row["COURSECODE"];
            $this->allData[$dataKey] = $row;
            $this->schoolRuikeiCourse[$row["EDBOARD_SCHOOLCD"]][$row["RUIKEI_DIV"]][$row["COURSECD"]][$row["MAJORCD"].$row["COURSECODE"]] =  $row["COURSENAME"];
        }
        $result->free();

        //titleArrayのLARGE_DIVにより出力するフィールドを決める為の配列
        //LARGE_DIV='01'は志願者数、LARGE_DIV='02'は覚書人数…
        //V_AFT_SINRO_ADDITION_L350W_DATのフィールド名と連動させる
        $largeDivField["98"][] = "NYUUGAKU_TEIIN";
        $largeDivField["99"][] = "BOSYUU_TEIIN";
        $largeDivField["01"][] = "SIGAN_M";
        $largeDivField["01"][] = "SIGAN_W";
        $largeDivField["01"][] = "SIGAN_T";
        $largeDivField["02"][] = "OBOEGAKI_M";
        $largeDivField["02"][] = "OBOEGAKI_W";
        $largeDivField["02"][] = "OBOEGAKI_T";
        $largeDivField["03"][] = "KAIGAI_M";
        $largeDivField["03"][] = "KAIGAI_W";
        $largeDivField["03"][] = "KAIGAI_T";
        $largeDivField["04"][] = "KISOTSU_M";
        $largeDivField["04"][] = "KISOTSU_W";
        $largeDivField["04"][] = "KISOTSU_T";
        $largeDivField["05"][] = "SEIJIN_M";
        $largeDivField["05"][] = "SEIJIN_W";
        $largeDivField["05"][] = "SEIJIN_T";
        $largeDivField["06"][] = "TSUUGAKUGAI_M";
        $largeDivField["06"][] = "TSUUGAKUGAI_W";
        $largeDivField["06"][] = "TSUUGAKUGAI_T";
        $largeDivField["07"][] = "SPORTTOKU_M";
        $largeDivField["07"][] = "SPORTTOKU_W";
        $largeDivField["07"][] = "SPORTTOKU_T";
        $largeDivField["08"][] = "GOUKAKU_M";
        $largeDivField["08"][] = "GOUKAKU_W";
        $largeDivField["08"][] = "GOUKAKU_T";

        /* 学校毎のコース単位で改ページ */
        $dataAri = false;
        $contents = "";
        foreach ($this->selectData as $key => $schoolCd) {
            //学校のコースを取得
            $schoolCoureArray = array();
            $query = knjl330wQuery::getSchoolCourse($this, $schoolCd);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $schoolCoureArray[] = $row;
            }
            $result->free();

            /* 学校毎のコース単位で改ページ */
            foreach ($schoolCoureArray as $courseKey => $course) {
                $setSchool = $setTestName.",".$this->schoolArray[$schoolCd].",".$course["COURSENAME"]."\n";
                $contents .= $setSchool;

                //累計区分毎に出力
                $setRuikei = "";
                foreach ($this->ruikeiArray as $ruikeiKey => $ruikeiVal) {
                    $setRuikei = $ruikeiVal["NAME1"]."\n";
                    $setTitleField = "学科・コース名";
                    $setSexField = "\n,,";
                    //項目名
                    foreach ($this->titleArray[$ruikeiKey] as $titleKey => $titleVal) {
                        if ($titleVal["LARGE_DIV"] != "98" && $titleVal["LARGE_DIV"] != "99") {
                            $setTitleField .= ",".$titleVal["LARGE_NAME"].",,";
                            //入学定員、募集定員以外は、性別毎の集計
                            $setSexField .= ",男子,女子,合計";
                        } else {
                            $setTitleField .= ",".$titleVal["LARGE_NAME"];
                        }
                    }

                    //学科コースとデータ
                    $setDataField = "";
                    $setDataTotal = array();    //合計用
                    if (is_array($this->schoolRuikeiCourse[$schoolCd][$ruikeiVal["VALUE"]][$course["COURSECD"]])) {
                        //学校＋累計＋課程にぶら下がる学科コース
                        foreach ($this->schoolRuikeiCourse[$schoolCd][$ruikeiVal["VALUE"]][$course["COURSECD"]] as $mcKey => $mcVal) {
                            $setDataField .= $mcVal;
                            foreach ($this->titleArray[$ruikeiKey] as $titleKey => $titleVal) {
                                $setKey = $schoolCd.$ruikeiVal["VALUE"].$course["COURSECD"].$mcKey;
                                foreach ($largeDivField[$titleVal["LARGE_DIV"]] as $largeKey => $largeVal) {
                                    $setDataField .= ",".$this->allData[$setKey][$largeVal];
                                    //合計用
                                    $setDataTotal[$largeVal] += $this->allData[$setKey][$largeVal];
                                }
                            }
                            $setDataField .= "\n";
                        }
                    }

                    //合計出力
                    $setDataField .= "全て";
                    foreach ($setDataTotal as $largeKey => $largeVal) {
                        $setDataField .= ",".$largeVal;
                    }
                    $setDataField .= "\n";
                    $contents .= $setRuikei.$setTitleField.$setSexField."\n{$setDataField}\n\n";
                    $dataAri = true;
                }
            }
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
