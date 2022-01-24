<?php

require_once('for_php7.php');

class knjb0055Form2 {
    function main(&$model) {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }
        $objForm = new form;
        $db         = Query::dbCheckOut();

        /************/
        /* タイトル */
        /************/
        if ($model->field["SUBCLASSCD"]) {
            $query = knjb0055Query::getSubclassName($model);
            $names = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if ($names) {
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $arg["TITLE"] = "{$model->field["SUBCLASSCD"]}　{$names["SUBCLASSNAME"]}";
                } else {
                    $classcd = substr($model->field["SUBCLASSCD"], 0, 2);
                    $arg["TITLE"] = "{$model->field["SUBCLASSCD"]}　{$names["SUBCLASSNAME"]}";
                }
            } else {
                $arg["TITLE"] = "";
            }
        }

        /**********/
        /* リスト */
        /**********/
        $query = knjb0055Query::getChairList($model);
        $result = $db->query($query);
        $existFlg = false; //この科目に講座があるのかのフラグ
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($chaircd == $row["CHAIRCD"]) {
                continue;
            }
            $row["APPDATE"]    = str_replace("-", "/", $row["APPDATE"]);
            $row["APPENDDATE"] = str_replace("-", "/", $row["APPENDDATE"]);
            if ($row["APPDATE"] && $row["APPENDDATE"]) {
                $row["KIKAN"] = $row["APPDATE"] . "～" . $row["APPENDDATE"];
            } else {
                $row["KIKAN"] = '';
            }
            if ($row["CHARGEDIV"] == '1') {
                $row["CHARGEDIV"] = '＊';
            } else {
                $row["CHARGEDIV"] = '';
            }

            $arg["data"][] = $row;

            $chaircd = $row["CHAIRCD"];
            $existFlg = true;
        }

        if ($model->field["SUBCLASSCD"] && !$existFlg) {
            $model->setMessage('この科目には講座がありません。');
        }

        /**************/
        /* ボタン作成 */
        /**************/
        //科目からの名簿入力
        $query = knjb0055Query::countChairStdDat($model);
        $cntSubclassStdDat = $db->getOne($query);//CHAIR_STD_DATのカウント
        $jumping = REQUESTROOT."/B/KNJB0056/knjb0056index.php";
        $extra = "onclick=\" Page_jumper('".$jumping."','".CTRL_YEAR."','".CTRL_SEMESTER."','".$model->field["SUBCLASSCD"]."','KNJB0055','".STAFFCD."', $cntSubclassStdDat);\"";
        if (!$existFlg) {
            $extra .= " disabled='disabled' ";
        }
        $arg["button"]["btn_exec"] = knjCreateBtn($objForm, 'btn_exec', '履修名簿から講座名簿作成', $extra);
        //講座作成
        $extra = "onclick=\"parent.location.href='../KNJB0030/knjb0030index.php?PROGRAMID=KNJB0055'\"";
        $arg["button"]["btn_subclass"] = knjCreateBtn($objForm, 'btn_subclass', '講座作成', $extra);
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, 'btn_end', '終 了', $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd", "");
        knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJB0055");
        knjCreateHidden($objForm, "SUBCLASSCD", $model->field["SUBCLASSCD"]);

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjb0055index.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjb0055Form2.html", $arg);
    }
}
?>
