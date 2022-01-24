<?php
require_once('knjd326kModel.inc');
require_once('knjd326kQuery.inc');

class knjd326kController extends Controller {
    var $ModelClassName = "knjd326kModel";
    var $ProgramID      = "KNJD326K";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "main":
                case "kakuend":
                    $this->callView("knjd326kForm1");
                    break 2;
                case "kaku":
                case "kakudai":
                    $this->callView("knjd326kForm1_1");
                    break 2;
                case "":
                    //分割フレーム作成
                    $search = "?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/D/KNJD326K/knjd326kindex.php?cmd=edit") ."&button=1";
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php" .$search;
                    $args["right_src"] = "knjd326kindex.php?cmd=edit";
                    $args["cols"] = "18%,82%";
                    View::frame($args);
                    return;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd326kCtl = new knjd326kController;
?>
