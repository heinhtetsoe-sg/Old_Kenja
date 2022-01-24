<?php

require_once('for_php7.php');

class knjh400_singakuForm1
{
    public function main(&$model)
    {
        $objForm      = new form();
        $arg["start"] = $objForm->get_start("edit", "POST", "knjh400_singakuindex.php", "", "edit");

        $db     = Query::dbCheckOut();

        $arg['SCHREGNO'] = $model->schregno;
        $arg['NAME'] = $db->getOne(knjh400_singakuQuery::getName($model));

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

        $result = $db->query(knjh400_singakuQuery::selectQuery($model));
        while ($row = $result->fetchrow(DB_FETCHMODE_ASSOC)) {
            //受験方式
            $query = knjh400_singakuQuery::getNameMst('E002');
            $row["HOWTOEXAM"] = makeCmb($objForm, $arg, $db, $query, "HOWTOEXAM", $row["HOWTOEXAM"], $extra, 1, 1);

            //受験結果
            $query = knjh400_singakuQuery::getNameMst('E005');
            $row["DECISION"] = makeCmb($objForm, $arg, $db, $query, "DECISION", $row["DECISION"], "", 1, 1);

            //進路状況
            $query = knjh400_singakuQuery::getNameMst('E006');
            $row["PLANSTAT"] = makeCmb($objForm, $arg, $db, $query, "PLANSTAT", $row["PLANSTAT"], "", 1, 1);

            //方式
            $query = knjh400_singakuQuery::getFormCd($row);
            $row["FORM_CD"] = makeCmb($objForm, $arg, $db, $query, "FORM_CD", $row["FORM_CD"], $extra, 1, 1);

            //日程
            $query = knjh400_singakuQuery::getNameMst('E044');
            $row["ADVERTISE_DIV"] = makeCmb($objForm, $arg, $db, $query, "ADVERTISE_DIV", $row["ADVERTISE_DIV"], $extra, 1, 1);

            $arg['data'][] = $row;
        }

        if ($model->Properties["Show_Recommendation"] == "1") {
            $arg["Show_Recommendation"] = 1;
        } else {
            $arg["Not_Show_Recommendation"] = 1;
        }

        //$model->entrydateは$row['ENTRYDATE']

        $result = $db->query(knjh400_singakuQuery::selectQuery2($model));
        while ($row = $result->fetchrow(DB_FETCHMODE_ASSOC)) {
            $row2 = $db->getRow(knjh400_singakuQuery::getSubQuery1($model, $row['ENTRYDATE'], $row['SEQ']), DB_FETCHMODE_ASSOC);
            for ($i=1; $i<=6; $i++) {
                $tmp = array();
                if ($i > 2) {
                    $row2 = $db->getRow(knjh400_singakuQuery::getSubQuery1Detail($model, $row['ENTRYDATE'], $row['SEQ'], $i), DB_FETCHMODE_ASSOC);
                }

                if ($model->Properties["Show_Recommendation"] == "1") {
                    $tmp["HOPE_NUM_LABEL"] = '第'.$i.'希望';
                } else {
                    $tmp["HOPE_NUM_LABEL"] = '第<br>'.common::PubFncKnjNumeral($i, 0).'<br>希<br>望';
                }

                //学校系列
                $query = knjh400_singakuQuery::getNameMst('E012');
                $tmp["SCHOOL_GROUP"] = makeCmb($objForm, $arg, $db, $query, "SCHOOL_GROUP".$i, $row2["SCHOOL_GROUP".$i], "", 1, 1);

                //学部系列
                $query = knjh400_singakuQuery::getFacultyGroup();
                $tmp["FACULTY_GROUP"] = makeCmb($objForm, $arg, $db, $query, "FACULTY_GROUP".$i, $row2["FACULTY_GROUP".$i], "", 1, 1);

                //学科系列
                $query = knjh400_singakuQuery::getDepartmentGroup();
                $tmp["DEPARTMENT_GROUP"] = makeCmb($objForm, $arg, $db, $query, "DEPARTMENT_GROUP".$i, $row2["DEPARTMENT_GROUP".$i], "", 1, 1);

                //学校情報
                $college1 = $db->getRow(knjh400_singakuQuery::getCollegeInfo($row2["SCHOOL_CD".$i], $row2["FACULTYCD".$i], $row2["DEPARTMENTCD".$i]), DB_FETCHMODE_ASSOC);

                //学校コード
                $tmp["SCHOOL_CD"] = $college1["SCHOOL_CD"];

                //学校名
                $tmp["SCHOOL_NAME"] = $college1["SCHOOL_NAME"];

                //学部名
                $tmp["FACULTYNAME"] = $college1["FACULTYNAME"];

                //学科名
                $tmp["DEPARTMENTNAME"] = $college1["DEPARTMENTNAME"];

                //受験区分
                $query = knjh400_singakuQuery::getNameMst('E002');
                $tmp["HOWTOEXAM"] = makeCmb($objForm, $arg, $db, $query, "HOWTOEXAM".$i, $row2["HOWTOEXAM".$i], "", 1, 1);

                $tmp["SCHOOL_GROUP_DISP"] = $tmp['SCHOOL_GROUP'].'　'.$tmp['FACULTY_GROUP'].'　'.$tmp['DEPARTMENT_GROUP'];
                if ($tmp["SCHOOL_GROUP_DISP"] == '　　') {
                    $tmp["SCHOOL_GROUP_DISP"] = '';
                }
                $tmp["SCHOOL_NAME_DISP"] = $tmp['SCHOOL_NAME'].'　'.$tmp['FACULTYNAME'].'　'.$tmp['DEPARTMENTNAME'];
                if ($tmp["SCHOOL_NAME_DISP"] == '　　') {
                    $tmp["SCHOOL_NAME_DISP"] = '';
                }

                $tmp["ID"] = $i;

                $row["col".$i] = $tmp;
            }

            $arg['data2'][] = $row;
        }

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, 'btn_back', '戻 る', $extra);

        knjCreateHidden($objForm, "cmd");

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjh400_singakuForm1.html", $arg);
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
