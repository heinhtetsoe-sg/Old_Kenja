<?php

require_once('for_php7.php');

/********************************************************************/
/* 変更履歴                                                         */
/* ･NO001：開始日の判定を追加                       山城 2004/11/17 */
/* ･NO002：登録更新後のデータ初期化処理修正         山城 2004/11/17 */
/********************************************************************/

class knjh211Form2
{
    public function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjh211index.php", "", "edit");

        //警告メッセージを表示しない場合
        if (isset($model->schregno) && isset($model->domicd) && isset($model->enterdate) && !isset($model->warning)) {
            $Row = knjh211Query::getRowSdate($model, $model->schregno, $model->domicd, $model->enterdate);   /* NO001 */
            $temp_cd = $Row["SCHREGNO"];
        } else {
            $Row =& $model->field;
        }

        //入寮日////////////////////////////////////////////////////////////////////////////////
        $arg["data"]["DOMI_ENTDAY"]=View::popUpCalendar($objForm, "DOMI_ENTDAY", str_replace("-", "/", $Row["DOMI_ENTDAY"]), "");


        //退寮日////////////////////////////////////////////////////////////////////////////////
        $arg["data"]["DOMI_OUTDAY"]=View::popUpCalendar($objForm, "DOMI_OUTDAY", str_replace("-", "/", $Row["DOMI_OUTDAY"]), "");


        //寮コードコンボボックスの中身を作成///////////////////////////////////////////////////////////////////////////////
        $db     = Query::dbCheckOut();      //dbCheckOut
        $query  = knjh211Query::getDomitory_Data($model, $model->control_data["年度"]);
        $result = $db->query($query);
        $opt_domicd = array();
        $opt_domicd[0] = array("label" => "",       "value" => "");
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_domicd[] = array("label" => htmlspecialchars($row["DOMI_NAME"]),
                          "value" => $row["DOMI_CD"]);
        }

        $result->free();
        Query::dbCheckIn($db);      //dbCheckIn

        $objForm->ae(array("type"        => "select",
                    "name"        => "DOMI_CD",
                    "size"        => 1,
                    "value"       => $Row["DOMI_CD"],
                    "options"     => $opt_domicd
                    ));

        $arg["data"]["DOMI_CD"] = $objForm->ge("DOMI_CD");


        //ボタン/////////////////////////////////////////////////////////////////////////////////////////////////////
        //追加ボタンを作成する
        $objForm->ae(array("type"        => "button",
                    "name"        => "btn_add",
                    "value"       => "登 録",
                    "extrahtml"   => "onclick=\"return btn_submit('add');\"" ));

        $arg["button"]["btn_add"] = $objForm->ge("btn_add");


        //修正ボタンを作成する
        $objForm->ae(array("type"        => "button",
                    "name"        => "btn_update",
                    "value"       => "更 新",
                    "extrahtml"   => "onclick=\"return btn_submit('update');\"" ));

        $arg["button"]["btn_update"] = $objForm->ge("btn_update");


        //削除ボタンを作成する
        $objForm->ae(array("type"        => "button",
                    "name"        => "btn_del",
                    "value"       => "削 除",
                    "extrahtml"   => "onclick=\"return btn_submit('delete');\"" ));

        $arg["button"]["btn_del"] = $objForm->ge("btn_del");


        //クリアボタンを作成する
        $objForm->ae(array("type"        => "reset",
                    "name"        => "btn_reset",
                    "value"       => "取 消",
                    "extrahtml"   => "onclick=\"return Btn_reset('edit');\"" ));

        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //終了ボタンを作成する
        $objForm->ae(array("type"        => "button",
                    "name"        => "btn_end",
                    "value"       => "終 了",
                    "extrahtml"   => "onclick=\"closeWin();\"" ));
                    
        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //CSVボタン
        $extra = "onClick=\" wopen('".REQUESTROOT."/X/KNJX_H211/knjx_h211index.php?SEND_PRGID=KNJH211','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        $arg["button"]["btn_csv"] = KnjCreateBtn($objForm, "btn_csv", "CSV入出力", $extra);

        //hiddenを作成する
        $objForm->ae(array("type"      => "hidden",
                    "name"      => "cmd"
                    ));

        //hiddenを作成する
        $objForm->ae(array("type"      => "hidden",
                    "name"      => "UPDATED",
                    "value"     => $Row["UPDATED"]
                    ));
                   
        $objForm->ae(array("type"      => "hidden",
                    "name"      => "SCHREGNO",
                    "value"     => $model->schregno
                    ));
        if ($temp_cd=="") {
            $temp_cd = $model->field["temp_cd"];
        }

        $objForm->ae(array("type"      => "hidden",
                    "name"      => "temp_cd",
                    "value"     => $temp_cd
                    ));
                                      
        $cd_change = false;
        if ($temp_cd==$Row["SCHREGNO"]) {
            $cd_change = true;
        }

        $arg["finish"]  = $objForm->get_finish();
        if (VARS::get("cmd") != "edit" && VARS::get("cmd") != "clear" && ($cd_change==true || $model->isload != 1) && !isset($model->warning)) {
            $arg["reload"]  = "window.open('knjh211index.php?cmd=list&SCHREGNO=$model->schregno','right_frame');";/* NO002 */
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh211Form2.html", $arg);
    }
}
