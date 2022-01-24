<?php
require_once('knjh830Model.inc');
require_once('knjh830Query.inc');

class knjh830Controller extends Controller {
    var $ModelClassName = "knjh830Model";
    var $ProgramID      = "KNJH830";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "change":
                case "hyouzi":
                    $this->callView("knjh830Form1");
                    break 2;
                case "csv":
                    if(!$sessionInstance->getCsvModel()){
                        $this->callView("knjh830Form1");
                    }
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/V/KNJVEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/V/KNJh830/knjh830index.php?cmd=edit")."&button=1";
                    $args["right_src"] = "knjh830index.php?cmd=edit";
                    $args["cols"] = "23%,*";
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
$KNJh830Ctl = new knjh830Controller;
//var_dump($_REQUEST);
?>
