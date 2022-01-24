<?php

require_once('for_php7.php');

class knjp741form1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjp741index.php", "", "right_list");

        $db = Query::dbCheckOut();

        $genKyuu = $db->getOne(knjp741Query::getGenkyuu());
        $schoolDiv = $db->getOne(knjp741Query::getSchoolMst());

        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = htmlspecialchars($model->name);
        $arg["ENT_DIV"]  = $db->getOne(knjp741Query::selectEnt($model->schregno));

        //ALLチェック
        $arg["CHECKALL"] = $this->createCheckBox($objForm, "CHECKALL", "", "onClick=\"return check_all(this);\"", "");

        $order[1] = "▲";
        $order[-1] = "▼";

        $arg["YEAR"] = View::alink("knjp741index.php", "<font color=\"white\">年度</font>", "",
                        array("cmd"=>"sort", "sort"=>"YEAR")) .$order[$model->sort["YEAR"]];

        $arg["SUBCLASSCD"] = View::alink("knjp741index.php", "<font color=\"white\">科目名</font>", "",
                        array("cmd"=>"sort", "sort"=>"SUBCLASSCD")) .$order[$model->sort["SUBCLASSCD"]];

        $zaisekiCol = 4;
        if ($model->Properties["useProvFlg"] == '1') {
            $arg["useProvFlg"] = "1";
            $zaisekiCol = 5;
        }
        $arg["ZAISEKI_COL"] = $zaisekiCol;
        //学籍賞罰データよりデータを取得
        $yearAnu = "";          //前データ年次
        $sub = "";          //前データ科目
        $firstflg = true;   //初回フラグ
        $setval = array();  //出力データ配列
        $totalCredit = 0;   //合計単位
        if($model->schregno)
        {
            $colorJudgeAr = array();
            $query = knjp741Query::selectQuery($model, "HENSYU", "CHECK_COLOR");
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $setSubClass = $row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"]."-".$row["SUBCLASSCD"];
                $colorJudgeAr[$setSubClass][$row["GRADE"]]["FLG"] = $row["YUUKOU_FLG"] == "1" ? "1" : $colorJudgeAr[$setSubClass][$row["GRADE"]]["FLG"];
                $colorJudgeAr[$setSubClass][$row["GRADE"]]["MAXYEAR"] = $row["YEAR"];
                $colorJudgeAr[$setSubClass][$row["GRADE"]]["RYUNEN"] = $row["RYUNEN_FLG"];
                $colorJudgeAr[$setSubClass][$row["GRADE"]]["YEAR"][$row["YEAR"]] = $row["YUUKOU_FLG"];
            }
            $result->free();

            $colorAr = array();
            foreach ($colorJudgeAr as $subCd => $subVal) {
                foreach ($subVal as $grade => $gradeVal) {
                    if ($schoolDiv == "1" && $genKyuu == "0") {
                        $colorAr[$yearKey][$subCd] = "white";
                    } else if ($gradeVal["FLG"] == "1") {
                        foreach ($gradeVal["YEAR"] as $yearKey => $yuukouFlg) {
                            if ($yuukouFlg == "1") {
                                $colorAr[$yearKey][$subCd] = "white";
                            } else {
                                $colorAr[$yearKey][$subCd] = "pink";
                            }
                        }
                    } else {
                        foreach ($gradeVal["YEAR"] as $yearKey => $yuukouFlg) {
                            if ($gradeVal["RYUNEN"] == "1") {
                                $colorAr[$yearKey][$subCd] = "pink";
                            } else if ($yearKey == $gradeVal["MAXYEAR"]) {
                                $colorAr[$yearKey][$subCd] = "white";
                            } else {
                                $colorAr[$yearKey][$subCd] = "pink";
                            }
                        }
                    }
                }
            }

            $result = $db->query(knjp741Query::selectQuery($model));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $setSubClass = $row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"]."-".$row["SUBCLASSCD"];
                $setColor = $colorAr[$row["YEAR"]][$setSubClass];

                $checksubcd = ($row["CHECKSUBCD"]) ? "_M" : "";
                $row["SUBCLASSNAME"] = $row["SUBCLASSNAME".$checksubcd];
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $row["CLASSCD_DISP"] = $row["CLASSCD"]."-".$row["SCHOOL_KIND"];
                    $row["CLASSCD"]     = $row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"]."-".substr($row["SUBCLASSCD"],0,2);
                    $row["SUBCLASSCD"]  = $row["CLASSCD"].substr($row["SUBCLASSCD"],2,6);
                }
                if ($firstflg || ($row["YEAR"].$row["ANNUAL"] == $yearAnu && $row["SUBCLASSCD"] == $sub)) {
                    //同一年次、科目のデータを連結(初回データ設定も含む)
                    $setval = $this->setData($row, $setval, $firstflg, $setColor, "join");
                    $totalCredit += $row["GET_CREDIT"] + $row["ADD_CREDIT"];
                    $firstflg = false;
                } else {
                    //前データ出力
                    $extraCheck = $this->getShomeiCheck($db, $setval["YEAR"], $model->schregno, $model->Properties["useSeitoSidoYorokuShomeiKinou"]);
                    $setval["CHECKED"] = $this->createCheckBox($objForm, "CHECKED", $setval["YEAR"] ."," .$setval["ANNUAL"] ."," .$setval["SUBCLASSCD"], $extraCheck, "1");
                    $setval["CREDIT"] = $totalCredit;
                    $arg["data"][] = $setval;
                    $totalCredit = 0;
                    $setval = array();
                    //現データ設定
                    $setval = $this->setData($row, $setval, $firstflg, $setColor);
                    $totalCredit += $row["GET_CREDIT"] + $row["ADD_CREDIT"];
                }
                $yearAnu = $row["YEAR"].$row["ANNUAL"];
                $sub = $row["SUBCLASSCD"];
            }
            $extraCheck = $this->getShomeiCheck($db, $setval["YEAR"], $model->schregno, $model->Properties["useSeitoSidoYorokuShomeiKinou"]);
            $setval["CHECKED"] = $this->createCheckBox($objForm, "CHECKED", $setval["YEAR"] ."," .$setval["ANNUAL"] ."," .$setval["SUBCLASSCD"], $extraCheck, "1");

            $setval["CREDIT"] = $totalCredit;
            $arg["data"][] = $setval;
        }
        Query::dbCheckIn($db);

        //削除ボタンを作成する
        $arg["btn_del"] = $this->createBtn($objForm, "btn_del", "削 除", "onclick=\"return btn_submit('delete');\"");

        //hiddenを作成する
        $objForm->ae($this->createHiddenAe("cmd"));
        //hiddenを作成する
        $objForm->ae($this->createHiddenAe("clear","0"));

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjp741Form1.html", $arg);
    }

    //署名チェック
    function getShomeiCheck($db, $year, $schregno, $useSeitoSidoYorokuShomeiKinou) {
        $extraCheck = "";
        if ($useSeitoSidoYorokuShomeiKinou == 1) {
            $query = knjp741Query::getOpinionsWk($year, $schregno);
            $check = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if ($check["CHAGE_OPI_SEQ"]) {
                $extraCheck = "disabled";
            }
        }
        return $extraCheck;
    }

    //
    function setData($row, &$setval, $firstflg, $setColor, $join = "") {

        if ($firstflg || $join != "join") {
            $setval = $row;
        }
        if ($row["PROV_FLG"]) {
            $setval["PROV_FLG".$row["SCHOOLCD"]]    = 'レ';
        }
        $setval["VALUATION".$row["SCHOOLCD"]]   = $row["VALUATION"];
        $setval["GET_CREDIT".$row["SCHOOLCD"]]  = $row["GET_CREDIT"];
        $setval["ADD_CREDIT".$row["SCHOOLCD"]]  = $row["ADD_CREDIT"];
        $setval["COMP_CREDIT".$row["SCHOOLCD"]] = $row["COMP_CREDIT"];
        $setval["COLOR"] = $setColor ? $setColor : "white";

        return $setval;
    }
    //ラジオ作成
    function createCheckBox(&$objForm, $name, $value, $extra, $multi) {

        $objForm->ae( array("type"      => "checkbox",
                            "name"      => $name,
                            "value"     => $value,
                            "extrahtml" => $extra,
                            "multiple"  => $multi));

        return $objForm->ge($name);
    }

    //ボタン作成
    function createBtn(&$objForm, $name, $value, $extra)
    {
        $objForm->ae( array("type"        => "button",
                            "name"        => $name,
                            "extrahtml"   => $extra,
                            "value"       => $value ) );
        return $objForm->ge($name);
    }

    //Hidden作成ae
    function createHiddenAe($name, $value = "")
    {
        $opt_hidden = array();
        $opt_hidden = array("type"      => "hidden",
                            "name"      => $name,
                            "value"     => $value);
        return $opt_hidden;
    }

}
?>
