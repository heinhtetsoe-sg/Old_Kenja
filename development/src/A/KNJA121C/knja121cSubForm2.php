<?php

require_once('for_php7.php');

require_once("AttendAccumulate.php");

class knja121cSubForm2
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform2", "POST", "knja121cindex.php", "", "subform2");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $arg["NAME_SHOW"] = $model->schregno."　".$model->name;

        //校種、学校コード
        $schoolcd = $school_kind = "";
        if ($db->getOne(knja121cQuery::checkSchoolMst()) > 0) {
            $schoolcd       = sprintf("%012d", SCHOOLCD);
            $school_kind    = $db->getOne(knja121cQuery::getSchoolKind($model));
        }

        //学校マスタ情報
        $knjSchoolMst = AttendAccumulate::getSchoolMstMap($db, $model->exp_year, $schoolcd, $school_kind);

        //出欠の記録
        for($i=1; $i<=$model->control["学期数"]; $i++) {
            $result = $db->query(knja121cQuery::getAttendSemesDat($model, $i, $knjSchoolMst));
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row["SEM_NAME"] = $model->control["学期名"][$i];
                $row["ATTENDREC_REMARK"] = knjCreateTextBox($objForm, $row["ATTENDREC_REMARK"], "ATTENDREC_REMARK", 40, 40, "readonly");

                $arg["data"][] = $row;
            }
            $result->free();
        }

        //戻るボタン
        $extra = "onclick=\"return top.main_frame.right_frame.closeit()\"";
        $arg["button"]["btn_back"] = KnjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja121cSubForm2.html", $arg);
    }
}
?>