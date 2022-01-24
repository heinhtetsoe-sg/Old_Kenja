<?php
class knjl730hForm2
{
    public function main(&$model)
    {
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl730hindex.php", "", "main");

        $db = Query::dbCheckOut();
        $row =& $model->field;

        //会場名
        $extra = "";
        $arg["EXAMHALL_NAME"] = knjCreateTextBox($objForm, $row["EXAMHALL_NAME"], "EXAMHALL_NAME", 20, 20, $extra);

        //試験内容 (1:筆記 2:面接 3:作文)
        $opt = array(1, 2, 3);
        $model->field["EXAM_TYPE"] = ($model->field["EXAM_TYPE"] == "") ? "1" : $model->field["EXAM_TYPE"];
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"EXAM_TYPE{$val}\" ");
        }
        $radioArray = knjCreateRadio($objForm, "EXAM_TYPE", $model->field["EXAM_TYPE"], $extra, $opt, count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //更新ボタン作成
        $extra = "onclick=\"return btn_submit('hallupdate')\"";
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "追 加", $extra);

        //戻るボタン作成
        // $extra = "onclick=\"top.main_frame.closeit()\"";
        $extra = "onclick=\"return closeMethod()\"";
        $arg["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "名前", VALUE);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl730hForm2.html", $arg);
    }
}
