<?php

require_once('for_php7.php');

class knjz300Form1
{
    public function main(&$model)
    {
        $objForm = new form();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjz300index.php", "", "edit");

        $db = Query::dbCheckOut();

        if ($model->isChgPwdUse) {
            $arg["chgPwd"] = 1;
        }

        //リンク先設定
        $link = REQUESTROOT."/Z/KNJZ300_2/knjz300_2index.php";
        $order[$model->sort["SRT_U"]]="";
        $order[$model->sort["SRT_S"]]="";
        //ソート表示文字作成
        $order[1] = "▲";
        $order[-1] = "▼";

        //リストヘッダーソート作成
        $STAFFCD_SORT = "<a href=\"knjz300index.php?cmd=list&sort=SRT_U&year=".$model->year."\" target=\"left_frame\" STYLE=\"color:white\">職員コード".$order[$model->sort["SRT_U"]]."</a>";

        $arg["STAFFCD_SORT"] = $STAFFCD_SORT;

        $USERKANA_SORT = "<a href=\"knjz300index.php?cmd=list&sort=SRT_S&year=".$model->year."\" target=\"left_frame\" STYLE=\"color:white\">職員氏名かな".$order[$model->sort["SRT_S"]]."</a>";

        $arg["USERKANA_SORT"] = $USERKANA_SORT;

        //コンボボックス内データ取得
        $query = knjz300Query::selectYearQuery();
        $result = $db->query($query);
        $cmb_opt = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $cmb_opt[] = array("label" => $row["YEAR"], "value" => $row["YEAR"]);
        }

        $result->free();

        //年度コンボボックスを作成する
        $extra = "onChange=\"return Cleaning();\"";
        $arg["year"]["VAL"] = knjCreateCombo($objForm, "year", $model->year, $cmb_opt, $extra, 1);

        //事前処理チェック(職員マスタの登録チェック)
        $query = knjz300Query::getCheckListQuery($model);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if (!is_array($row)) {
            $arg["close"] = " closing_window(); " ;
        }
        //リスト内データ取得
        $query = knjz300Query::readQuery($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");

            if ($row["ENT_ADMIN"] == "1") {     //入試管理者
                $row["Link_CD"] = $row["STAFFCD"];
            } else {
                $row["Link_CD"] = View::alink(
                    "knjz300index.php",
                    $row["STAFFCD"],
                    "target=\"right_frame\"",
                    array("cmd"     => "edit",
                                                    "USERSCD" => $row["STAFFCD"],
                                                    "YEAR"    => $model->year)
                );
            }

            //リンクの有無設定
            if ($row["USERID"]) {
                $row["LINKER"] = View::alink(
                    $link,
                    "設定",
                    "target=\"_parent\"",
                    array("USERSCD" => $row["STAFFCD"],
                                                   "YEAR" => $model->year)
                );
            } else {
                $row["LINKER"] = "設定";
            }

            if ($row["ENT_ADMIN"] == "1") {     //入試管理者
                $row["USERID"] = '';
            }
            $row["INVALID_FLG"] = ($row["INVALID_FLG"] == '1') ? "アカウント無効" : "";
            $row["PWDTERMCHK_FLG"] = ($row["PWDTERMCHK_FLG"] == '1') ? "期限あり" : "";
            $row["CHG_PWD_FLG"] = ($row["CHG_PWD_FLG"] == '1') ? "次回変更" : "";

            //更新後この行が画面の先頭に来るようにする
            if ($row["STAFFCD"] == $model->userscd2) {
                unset($model->userscd2);
                $row["STAFFNAME"] = ($row["STAFFNAME"]) ? $row["STAFFNAME"] : "　";
                $row["STAFFNAME"] = "<a name=\"target\">{$row["STAFFNAME"]}</a><script>location.href='#target';</script>";
            }

            $arg["data"][] = $row;
        }

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SEND_selectSchoolKind", $model->selectSchoolKind);

        //職員別所属グループ確認ボタンを作成する
        $link1 = REQUESTROOT."/X/KNJXUSRGRPLST/knjxusrgrplstindex.php";
        $extra = "style=\"width:200px\"onclick=\" Page_jumper('".$link1."','1');\"";
        $arg["auth_check"] = knjCreateBtn($objForm, "auth_check", "職員別所属グループ確認", $extra);

        if ($model->sec_competence != DEF_UPDATABLE) {
            $arg["close"] = " closing_window(1); " ;
        }

        if ($model->cmd == "change_kind") {
            $arg["jscript"] = "window.open('knjz300index.php?cmd=edit','right_frame');";
        }

        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "window.open('knjz300index.php?cmd=list','left_frame');";
        }

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz300Form1.html", $arg);
    }
}
