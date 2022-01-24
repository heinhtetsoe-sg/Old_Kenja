<?php

require_once('for_php7.php');

require_once('knjl375qModel.inc');
require_once('knjl375qQuery.inc');

class knjl375qController extends Controller {
    var $ModelClassName = "knjl375qModel";
    var $ProgramID      = "KNJL375Q";
	
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit";    //下
                    $this->callView("knjl375qForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $this->callView("knjl375qForm1");
                    break 2;
               case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["right_src"] = "knjl375qindex.php?cmd=list";
                    $args["edit_src"] = "knjl375qindex.php?cmd=edit&KUBUN=1&KAMOKU=1&KAISU=1";
                    $args["rows"] = "30%,*";
                    View::frame($args,  "frame3.html");
                    exit;

                case "list";    //上
                case "list2";    //更新後下フレームをリロードする
                case "change";
                    $this->callView("knjl375qForm2");
                    break 2;
                case "import":
                    if($sessionInstance->getImportModel()){
                        $sessionInstance->setCmd("list2");
                        break 1;
                    }else{
                        $this->callView("knjl375qForm2");
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
$knjl375qCtl = new knjl375qController;
?>
