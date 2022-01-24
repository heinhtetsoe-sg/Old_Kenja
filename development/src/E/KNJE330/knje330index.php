<?php

require_once('for_php7.php');

// kanji=漢字
// $Id: knje330index.php 69350 2019-08-23 08:44:42Z ishii $

require_once('knje330Model.inc');
require_once('knje330Query.inc');

class knje330Controller extends Controller {
    var $ModelClassName = "knje330Model";
    var $ProgramID      = "KNJE330";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "init":
                case "clear":
                case "list":
                    $this->callView("knje330Form1");
                    break 2;

                case "search":
// debug echo "index:".date("r")."[".$sessionInstance->field[STAT_CD]."]<br>";
                    $sessionInstance->chkCollegeMst($sessionInstance->field["STAT_CD"]);
                    // 'edit' へ
                case "edit":
                    $this->callView("knje330Form2");
                    break 2;

                case "right_list":
                case "from_list":
                    $this->callView("knje330Form1");
                    break 2;

                case "add":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getInsertModel();
                    $sessionInstance->setCmd("edit");
                    break 1;

                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;

                case "delete":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;

                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;

                case "error":
                    $this->callView("error");
                    break 2;

                case "":
                    $sessionInstance->knje330Model();

                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/E/KNJE330/knje330index.php?cmd=right_list") ."&button=3";
                    $args["right_src"] = "knje330index.php?cmd=right_list";
                    $args["edit_src"]  = "knje330index.php?cmd=edit";
                    $args["cols"] = "25%,75%";
                    $args["rows"] = "30%,70%";
                    View::frame($args,"frame2.html");
                    return;

                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knje330Ctl = new knje330Controller;
?>
