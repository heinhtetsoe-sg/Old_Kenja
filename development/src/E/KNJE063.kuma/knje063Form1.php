<?php

require_once('for_php7.php');

class knje063form1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knje063index.php", "", "right_list");

        $db = Query::dbCheckOut();

        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = htmlspecialchars($model->name);
        $arg["ENT_DIV"]  = $db->getOne(knje063Query::selectEnt($model->schregno));

        //ALLチェック
        $arg["CHECKALL"] = $this->createCheckBox($objForm, "CHECKALL", "", "onClick=\"return check_all(this);\"", "");

        $order[1] = "▲";
        $order[-1] = "▼";

        $arg["YEAR"] = View::alink("knje063index.php", "<font color=\"white\">年度</font>", "",
                        array("cmd"=>"sort", "sort"=>"YEAR")) .$order[$model->sort["YEAR"]];

        $arg["SUBCLASSCD"] = View::alink("knje063index.php", "<font color=\"white\">科目名</font>", "",
                        array("cmd"=>"sort", "sort"=>"SUBCLASSCD")) .$order[$model->sort["SUBCLASSCD"]];

        //学籍賞罰データよりデータを取得
        $yearAnu = "";          //前データ年次
        $sub = "";          //前データ科目
        $firstflg = true;   //初回フラグ
        $setval = array();  //出力データ配列
        $totalCredit = 0;   //合計単位
        if($model->schregno)
        {
            $result = $db->query(knje063Query::selectQuery($model));
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
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
                    $setval = $this->setData($row, $setval, $firstflg, "join");
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
                    $setval = $this->setData($row, $setval, $firstflg);
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
        View::toHTML($model, "knje063Form1.html", $arg);
    }

    //署名チェック
    function getShomeiCheck($db, $year, $schregno, $useSeitoSidoYorokuShomeiKinou) {
        $extraCheck = "";
        if ($useSeitoSidoYorokuShomeiKinou == 1) {
            $query = knje063Query::getOpinionsWk($year, $schregno);
            $check = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if ($check["CHAGE_OPI_SEQ"]) {
                $extraCheck = "disabled";
            }
        }
        return $extraCheck;
    }

    //
    function setData($row, &$setval, $firstflg, $join = "") {

        if ($firstflg || $join != "join") {
            $setval = $row;
        }
        $setval["VALUATION".$row["SCHOOLCD"]]   = $row["VALUATION"];
        $setval["GET_CREDIT".$row["SCHOOLCD"]]  = $row["GET_CREDIT"];
        $setval["ADD_CREDIT".$row["SCHOOLCD"]]  = $row["ADD_CREDIT"];
        $setval["COMP_CREDIT".$row["SCHOOLCD"]] = $row["COMP_CREDIT"];

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
