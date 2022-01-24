<?php

require_once('for_php7.php');

require_once('knjl372qModel.inc');
require_once('knjl372qQuery.inc');

class knjl372qController extends Controller {
    var $ModelClassName = "knjl372qModel";
    var $ProgramID      = "KNJl372q";
    
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                    $this->callView("knjl372qForm2");
                    break 2;
                case "school_csv":
                    if(!$sessionInstance->getCsvModel()){
                        $this->callView("knjl372qForm1");
                    }
                    break 2;
                case "list":
                    $this->callView("knjl372qForm1");
                    break 2;
                case "group_csv":
                    if(!$sessionInstance->getCsvModel()){
                        $this->callView("knjl372qForm2");
                    }
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "top":
                    $this->callView("knjl372qForm3");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["top_src"] = "knjl372qindex.php?cmd=top";
                    $args["left_src"] = "knjl372qindex.php?cmd=list";
                    $args["right_src"] = "knjl372qindex.php?cmd=edit";
                    $args["cols"] = "50%,50%";
                    $args["rows"] = "0%,10%,*";
                    View::frame($args,  "frame4.html");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
            
        }
    }
}
$knjl372qCtl = new knjl372qController;
//var_dump($_REQUEST);
?>
