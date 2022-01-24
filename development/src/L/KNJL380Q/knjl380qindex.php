<?php

require_once('for_php7.php');

require_once('knjl380qModel.inc');
require_once('knjl380qQuery.inc');

class knjl380qController extends Controller {
    var $ModelClassName = "knjl380qModel";
    var $ProgramID      = "KNJL380Q";
	
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                    $sessionInstance->getUpdateModel();
                    $this->callView("knjl380qForm1");
                    break 2;
                case "delete":
                    $sessionInstance->getDeleteModel();
                    $this->callView("knjl380qForm1");
                    break 2;
                case "copy":
                    $sessionInstance->getCopyModel();
                    $this->callView("knjl380qForm1");
                    break 2;
               case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["right_src"] = "knjl380qindex.php?cmd=list";
                    $args["edit_src"] = "knjl380qindex.php?cmd=edit";
                    $args["rows"] = "30%,*";
                    View::frame($args,  "frame3.html");
                    exit;
                case "edit";    //下
                    $this->callView("knjl380qForm1");
                    break 2;

                case "list";    //上
                case "list2";
                    $this->callView("knjl380qForm2");
                    break 2;
                case "insert":
                    if($sessionInstance->getInsertModel()){
                        $sessionInstance->setCmd("list2");
                        break 1;
                    }else{
                        $this->callView("knjl380qForm2");
                        break 2;
                    }
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
            
        }
    }
}
$knjl380qCtl = new knjl380qController;
?>
