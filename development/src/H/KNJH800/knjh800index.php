<?php
require_once('knjh800Model.inc');
require_once('knjh800Query.inc');

class knjh800Controller extends Controller {
    var $ModelClassName = "knjh800Model";
    var $ProgramID      = "KNJH800";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "change":
                case "hyouzi":
                    $this->callView("knjh800Form1");
                    break 2;
                case "csv":
                    if(!$sessionInstance->getCsvModel()){
                        $this->callView("knjh800Form1");
                    }
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/V/KNJVEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/H/KNJh800/knjh800index.php?cmd=edit")."&button=1";
                    $args["right_src"] = "knjh800index.php?cmd=edit";
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
$KNJh800Ctl = new knjh800Controller;
//var_dump($_REQUEST);
?>
