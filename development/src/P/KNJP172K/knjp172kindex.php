<?php

require_once('for_php7.php');

require_once('knjp172kModel.inc');
require_once('knjp172kQuery.inc');

class knjp172kController extends Controller {
    var $ModelClassName = "knjp172kModel";
    var $ProgramID      = "KNJP172K";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "clear":
                    $this->callView("knjp172kForm1");
                    break 2;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/P/KNJP172K/knjp172kindex.php?cmd=edit") ."&button=1";
                    $args["right_src"] = "knjp172kindex.php?cmd=edit";
                    $args["cols"] = "50%,50%";
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
$knjp172kCtl = new knjp172kController;
?>
