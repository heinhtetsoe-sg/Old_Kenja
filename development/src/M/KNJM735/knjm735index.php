<?php

require_once('for_php7.php');
require_once('knjm735Model.inc');
require_once('knjm735Query.inc');

class knjm735Controller extends Controller {
    var $ModelClassName = "knjm735Model";
    var $ProgramID      = "KNJM735";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "search":
                    $this->callView("knjm735Form1");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("search");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"]  = REQUESTROOT ."/X/KNJXEXP4/index.php?";
                    $args["left_src"] .= "PROGRAMID=".$this->ProgramID;
                    $args["left_src"] .= "&search_div=1";
                    $args["left_src"] .= "&name=1";
                    $args["left_src"] .= "&name_kana=1";
                    $args["left_src"] .= "&ent_year=1";
                    $args["left_src"] .= "&grd_year=1";
                    $args["left_src"] .= "&grade=1";
                    $args["left_src"] .= "&hr_class=1";
                    $args["left_src"] .= "&a028=1";
                    $args["left_src"] .= "&paid_umu=1";
                    $args["left_src"] .= "&schno=1";
                    $args["left_src"] .= "&searchMode=send";
                    $args["left_src"] .= "&TARGET=right_frame";
                    $args["left_src"] .= "&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}";

                    $args["left_src"] .= "&PATH=" .urlencode("/M/KNJM735/knjm735index.php?cmd=edit");

                    $args["right_src"] = "knjm735index.php?cmd=edit";;

                    $args["cols"] = "27%,*";
                    View::frame($args);
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$KNJM735Ctl = new knjm735Controller;
//var_dump($_REQUEST);
?>
