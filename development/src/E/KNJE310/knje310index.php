<?php

require_once('for_php7.php');

// kanji=漢字
// $Id: knje310index.php 69350 2019-08-23 08:44:42Z ishii $

require_once('knje310Model.inc');
require_once('knje310Query.inc');

class knje310Controller extends Controller {
    var $ModelClassName = "knje310Model";
    var $ProgramID      = "KNJE310";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "init":
                case "clear":
                case "list":
                    $this->callView("knje310Form1");
                    break 2;

                case "search":
                    $sessionInstance->chkCollegeOrCompanyMst($sessionInstance->field["STAT_CD"], $sessionInstance->field["SCHOOL_SORT"]);
                    // 'edit' へ
                case "edit":
                    $this->callView("knje310Form2");
                    break 2;

                case "right_list":
                case "from_list":
                    $this->callView("knje310Form1");
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
                    $sessionInstance->knje310Model();

                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/E/KNJE310/knje310index.php?cmd=right_list") ."&button=3";
                    $args["right_src"] = "knje310index.php?cmd=right_list";
                    $args["edit_src"]  = "knje310index.php?cmd=edit";
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
$knje310Ctl = new knje310Controller;
?>
