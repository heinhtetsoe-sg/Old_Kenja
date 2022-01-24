<?php

require_once('for_php7.php');

class knjh400_syuusyokuForm1
{
    public function main(&$model)
    {
        $objForm      = new form();
        $arg["start"] = $objForm->get_start("edit", "POST", "knjh400_syuusyokuindex.php", "", "edit");

        $db     = Query::dbCheckOut();

        $arg['SCHREGNO'] = $model->schregno;
        $arg['NAME'] = $db->getOne(knjh400_syuusyokuQuery::getName($model));

        $opt_gouhi = array(1, 2, 3);
        $model->gouhi = ($model->gouhi == "") ? "1" : $model->gouhi;
        $extra = array("id=\"GOUHI1\" onClick=\"btn_submit('edit')\"", "id=\"GOUHI2\" onClick=\"btn_submit('edit')\"", "id=\"GOUHI3\" onClick=\"btn_submit('edit')\"");
        $radioArray = knjCreateRadio($objForm, "GOUHI", $model->gouhi, $extra, $opt_gouhi, get_count($opt_gouhi));
        foreach ($radioArray as $key => $val) {
            $arg[$key] = $val;
        }
        $opt_sinro = array(1, 2);
        $model->sinro = ($model->sinro == "") ? "1" : $model->sinro;
        $extra = array("id=\"SINRO1\" onClick=\"btn_submit('edit')\"", "id=\"SINRO2\" onClick=\"btn_submit('edit')\"");
        $radioArray = knjCreateRadio($objForm, "SINRO", $model->sinro, $extra, $opt_sinro, get_count($opt_sinro));
        foreach ($radioArray as $key => $val) {
            $arg[$key] = $val;
        }

        $result = $db->query(knjh400_syuusyokuQuery::selectQuery($model));
        while ($row = $result->fetchrow(DB_FETCHMODE_ASSOC)) {
            // 会社情報
            $company = $db->getrow(knjh400_syuusyokuQuery::getCollegeOrCompanyMst(trim($row["STAT_CD"])), DB_FETCHMODE_ASSOC);

            //産業種別
            $row["INDUSTRY_MNAME"] = $company["INDUSTRY_MNAME"];

            //所在地
            $row["PREF_CD"] = ($row["CITY_CD"]) ? $row["PREF_CD"].'-'.$row["CITY_CD"] : $row["PREF_CD"].'-';
            $query = knjh400_syuusyokuQuery::getPrefList($mainpref);
            $row["PREF_CD"] = makeCmb($objForm, $arg, $db, $query, "PREF_CD", $row["PREF_CD"], "", 1, 1);

            //受験結果
            $query = knjh400_syuusyokuQuery::getNameMst('E005');
            $row["DECISION"] = makeCmb($objForm, $arg, $db, $query, "DECISION", $row["DECISION"], "", 1, 1);

            //進路状況
            $query = knjh400_syuusyokuQuery::getNameMst('E006');
            $row["PLANSTAT"] = makeCmb($objForm, $arg, $db, $query, "PLANSTAT", $row["PLANSTAT"], "", 1, 1);

            $arg['data'][] = $row;
        }
        $result = $db->query(knjh400_syuusyokuQuery::selectQuery2($model));
        while ($row = $result->fetchrow(DB_FETCHMODE_ASSOC)) {
            $query = knjh400_syuusyokuQuery::getJobtypeLList();
            $row["JOBTYPE_LCD1"] = makeCmb($objForm, $arg, $db, $query, "JOBTYPE_LCD1", $row["JOBTYPE_LCD1"], "", 1, 1);

            //職業種別（小分類）
            //$arg["data"]["JOBTYPE_SNAME1"] = $row["JOBTYPE_SNAME1"];

            //就業場所
            if ($row["WORK_AREA1"] == '1') {
                $row["WORK_AREA1"] = '県内';
            }
            if ($row["WORK_AREA1"] == '2') {
                $row["WORK_AREA1"] = '県外';
            }

            //紹介区分ラジオボタン 1:学校紹介 2:自己・縁故 3.公務員
            if ($row["INTRODUCTION_DIV1"] == '1') {
                $row["INTRODUCTION_DIV1"] = '学校紹介';
            }
            if ($row["INTRODUCTION_DIV1"] == '2') {
                $row["INTRODUCTION_DIV1"] = '自己・縁故';
            }
            if ($row["INTRODUCTION_DIV1"] == '1') {
                $row["INTRODUCTION_DIV1"] = '公務員';
            }

            $query = knjh400_syuusyokuQuery::getJobtypeLList();
            $row["JOBTYPE_LCD2"] = makeCmb($objForm, $arg, $db, $query, "JOBTYPE_LCD2", $row["JOBTYPE_LCD2"], "", 1, 1);

            //職業種別（小分類）
            //$arg["data"]["JOBTYPE_SNAME2"] = $row["JOBTYPE_SNAME2"];

            //就業場所
            if ($row["WORK_AREA2"] == '1') {
                $row["WORK_AREA2"] = '県内';
            }
            if ($row["WORK_AREA2"] == '2') {
                $row["WORK_AREA2"] = '県外';
            }

            //紹介区分ラジオボタン 1:学校紹介 2:自己・縁故 3.公務員
            if ($row["INTRODUCTION_DIV2"] == '1') {
                $row["INTRODUCTION_DIV2"] = '学校紹介';
            }
            if ($row["INTRODUCTION_DIV2"] == '2') {
                $row["INTRODUCTION_DIV2"] = '自己・縁故';
            }
            if ($row["INTRODUCTION_DIV2"] == '3') {
                $row["INTRODUCTION_DIV2"] = '公務員';
            }

            $arg['data2'][] = $row;
        }

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, 'btn_back', '戻 る', $extra);

        knjCreateHidden($objForm, "cmd");

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjh400_syuusyokuForm1.html", $arg);
    }
}
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $space = "")
{
    $result1 = $db->query($query);
    while ($row1 = $result1->fetchrow(DB_FETCHMODE_ASSOC)) {
        if ($value == $row1["VALUE"]) {
            $label = $row1["LABEL"];
        }
    }
    return $label;
}
