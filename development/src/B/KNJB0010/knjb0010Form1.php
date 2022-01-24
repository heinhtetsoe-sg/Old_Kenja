<?php

require_once('for_php7.php');

class knjb0010Form1
{
    public $dataRow = array(); //表示用一行分データをセット

    public function main(&$model)
    {
        $arg["jscript"] = "";
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }
        $objForm = new form();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjb0010index.php", "", "edit");

        $db     = Query::dbCheckOut();

        $arg["YEAR"]     = CTRL_YEAR."年度";
        $arg["SEMESTER"] = CTRL_SEMESTERNAME;

        $objForm->ae(array("type"        => "button",
                            "name"        => "copy_btn",
                            "value"       => "前学期からコピー",
                            "extrahtml"   => "onclick=\"btn_submit('copy');\"" ));
        $arg["copy_btn"] = $objForm->ge("copy_btn");

        $result = $db->query(knjb0010Query::SelectQuery($model, 0));

        //事前処理チェック
        if ($result->numRows() == 0) {
            $arg["jscript"] = "OnPreError('選択科目マスタ');";
        }

        $this->DataRow = array();
        for ($i=0; $row=$result->fetchRow(DB_FETCHMODE_ASSOC); $i++) {
            if ($i == 0) {
                $cd[] = $row["GROUPCD"];
            }

            //群コードが同じ間は表示しない
            if (in_array($row["GROUPCD"], $cd)) {
                $this->setDataRow($row, $model);
            } else {
                $this->modifyDataRow();
                $arg["data"][] = $this->DataRow;

                $cd = $this->DataRow = array();
                $cd[] = $row["GROUPCD"];
                $this->setDataRow($row, $model);
            }
        }
        $this->modifyDataRow();
        $arg["data"][] = $this->DataRow;

        //hidden
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "cmd"));

        if ($model->cmd == "combo") {
            $arg["jscript"] = "window.open('knjb0010index.php?cmd=edit','right_frame')";
        }

        $arg["finish"]  = $objForm->get_finish();

        $result->free();
        Query::dbCheckIn($db);

        View::toHTML($model, "knjb0010Form1.html", $arg);
    }

    //表示用配列にデータ値をセット
    public function setDataRow($row, $model)
    {
        $this->DataRow["GROUPCD"][]   = View::alink(
            "knjb0010index.php",
            $row["GROUPCD"],
            "target=right_frame",
            array("GROUPCD" => $row["GROUPCD"],
                                        "cmd"     => "edit",
                                        "NAME"    => $row["GROUPNAME"])
        );

        //更新後この行が画面の先頭に来るようにする
        if ($row["GROUPCD"] == $model->groupcd) {
            $row["GROUPNAME"] = ($row["GROUPNAME"]) ? $row["GROUPNAME"] : "　";
            $row["GROUPNAME"] = "<a name=\"target\">{$row["GROUPNAME"]}</a><script>location.href='#target';</script>";
        }

        //値が不揃いの場合は"*"を付ける
        $this->DataRow["LESSONCNT"][] = ((int)$row["L_CNT"] > 1) ? $row["LESSONCNT"]."*" : $row["LESSONCNT"];
        $this->DataRow["FRAMECNT"][]  = ((int)$row["F_CNT"] > 1) ? $row["FRAMECNT"]."*" : $row["FRAMECNT"];
        $this->DataRow["GROUPNAME"][] = $row["GROUPNAME"];
        $this->DataRow["CHAIRCD"][]   = $row["CHAIRCD"]." ".$row["CHAIRNAME"];
        $this->DataRow["HR_NAME"][]   = $row["HR_NAME"];
    }

    //表示用配列を表示できるように修正
    public function modifyDataRow()
    {
        if (isset($this->DataRow["GROUPCD"])) {
            $this->DataRow["GROUPCD"]   = implode("<BR>", array_unique($this->DataRow["GROUPCD"]));
        }
        if (isset($this->DataRow["LESSONCNT"])) {
            $this->DataRow["LESSONCNT"] = implode("<BR>", array_unique($this->DataRow["LESSONCNT"]));
        }
        if (isset($this->DataRow["FRAMECNT"])) {
            $this->DataRow["FRAMECNT"]  = implode("<BR>", array_unique($this->DataRow["FRAMECNT"]));
        }
        if (isset($this->DataRow["GROUPNAME"])) {
            $this->DataRow["GROUPNAME"] = implode("<BR>", array_unique($this->DataRow["GROUPNAME"]));
        }
        if (isset($this->DataRow["CHAIRCD"])) {
            $this->DataRow["CHAIRCD"]   = implode("<BR>", array_unique($this->DataRow["CHAIRCD"]));
        }
        if (isset($this->DataRow["HR_NAME"])) {
            $this->DataRow["HR_NAME"]   = implode("<BR>", array_unique($this->DataRow["HR_NAME"]));
        }
    }
}
