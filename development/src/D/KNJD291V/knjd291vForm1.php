<?php

require_once('for_php7.php');


class knjd291vForm1
{
    function main(&$model){

      //オブジェクト作成
      $objForm = new form;

      //フォーム作成
      $arg["start"]   = $objForm->get_start("knjd291vForm1", "POST", "knjd291vindex.php", "", "knjd291vForm1");

      //年度
      $arg["data"]["YEAR"] = $model->control["年度"];

      //処理日を作成する
      $arg["data"]["DATE"] = View::popUpCalendar($objForm, "DATE", $model->field["DATE"] ? $model->field["DATE"] : str_replace("-", "/", CTRL_DATE));

      //出力パターン
      $opt = array(1, 2, 3);
      $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
      $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"", "id=\"OUTPUT3\"");
      $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, get_count($opt));
      foreach($radioArray as $key => $val) $arg["data"][$key] = $val;
 
      //印刷ボタンを作成する
      $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
      $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
      //終了ボタンを作成する
      $extra = "onclick=\"closeWin();\"";
      $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

      //学期期間日付取得////////////////////////////////////////////////////////////////////////////
      $semester = $model->control['学期開始日付'][1] ."," .$model->control['学期終了日付'][1];
      $semester = $semester ."," .$model->control['学期開始日付'][2] ."," .$model->control['学期終了日付'][2];
      $semester = $semester ."," .$model->control['学期開始日付'][3] ."," .$model->control['学期終了日付'][3];

      knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
      knjCreateHidden($objForm, "PRGID", "KNJD291V");
      knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
      knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
      knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
      knjCreateHidden($objForm, "SEMESTER");
      knjCreateHidden($objForm, "SEME_DATE", $semester);
      knjCreateHidden($objForm, "cmd");

      //フォーム終わり
      $arg["finish"]  = $objForm->get_finish();

      //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
      View::toHTML($model, "knjd291vForm1.html", $arg); 
  }
}
?>
