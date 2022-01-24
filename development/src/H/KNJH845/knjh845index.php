<?php
require_once('knjh845Model.inc');
require_once('knjh845Query.inc');

class knjh845Controller extends Controller {
    var $ModelClassName = "knjh845Model";
    var $ProgramID      = "KNJH845";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/V/KNJVEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/V/KNJH845/knjh845index.php?cmd=edit")."&button=1";
                    $args["right_src"] = "knjh845index.php?cmd=edit";
                    $args["cols"] = "23%,*";
                    View::frame($args);
                    exit;

                case "edit":
                case "change":
                case "hyouzi":
                case "kubun_change":
                case "kaisu_change":
                
                case "subclasscd":
                case "chaircd":
                    $this->callView("knjh845Form1");
                    break 2;
                case "csv":
                    if(!$sessionInstance->getCsvModel()){
                        $this->callView("knjh845Form1");
                    }
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$KNJh845Ctl = new knjh845Controller;
//var_dump($_REQUEST);
?>
