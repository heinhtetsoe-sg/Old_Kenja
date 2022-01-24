<?php

require_once('for_php7.php');

require_once('knjl040hModel.inc');
require_once('knjl040hQuery.inc');

class knjl040hController extends Controller {
    var $ModelClassName = "knjl040hModel";
    var $ProgramID      = "KNJL040H";     //プログラムID

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "read":
                case "back":
                case "next":
                case "reset":
                    $sessionInstance->getMainModel();
                    $this->callView("knjl040hForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    if ($sessionInstance->warning) {
                       echo "<script language=\"javascript\"> alert('".$sessionInstance->warning."');</script>";
                    } elseif ($sessionInstance->message) {
                       echo "<script language=\"javascript\"> alert('".$sessionInstance->message."');";
                       echo "top.main_frame.bottom_frame.location.href = '".REQUESTROOT."/L/KNJL040H/knjl040hindex.php?cmd=read';";
                       echo "</script>";
                    }
                    unset($sessionInstance->warning);
                    unset($sessionInstance->message);
                    
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["right_src"] = "";
                    $args["edit_src"]  = "knjl040hindex.php?cmd=main";
                    $args["rows"] = "0%,*";
                    View::frame($args, "frame3.html",0);
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl040hCtl = new knjl040hController;
?>
