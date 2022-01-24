<?php
require_once('knjb0030oModel.inc');
require_once('knjb0030oQuery.inc');

class knjb0030oController extends Controller {
    var $ModelClassName = "knjb0030oModel";
    var $ProgramID      = "KNJB0030O";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "group":
                case "edit":
                case "reset":
                    $this->callView("knjb0030oForm2");
                    break 2;
                case "subform1": //受講クラス
                    $this->callView("knjb0030oSubForm1");
                    break 2;
                case "subform2": //科目担任
                    $this->callView("knjb0030oSubForm2");
                    break 2;
                case "subform3": //使用施設
                    $this->callView("knjb0030oSubForm3");
                    break 2;
                case "subform4": //教科書
                    $this->callView("knjb0030oSubForm4");
                    break 2;
                case "insert":  //追加---2004.04.22
                case "update":  //更新
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "copy":  //コピー
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getInsertCopyModel();
                    $sessionInstance->setCmd("list");
                    break 1;
                case "sort":
                case "list":
                    $this->callView("knjb0030oForm1");
                    break 2;
                case "delete":  //削除
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjb0030oindex.php?cmd=list";
                    $args["right_src"] = "knjb0030oindex.php?cmd=edit";
                    $args["cols"] = "52%,48%";
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
$knjb0030oCtl = new knjb0030oController;
//var_dump($_REQUEST);
?>
