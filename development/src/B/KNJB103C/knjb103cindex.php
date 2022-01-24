<?php

require_once('for_php7.php');

require_once('knjb103cModel.inc');
require_once('knjb103cQuery.inc');

class knjb103cController extends Controller
{
    public $ModelClassName = "knjb103cModel";
    public $ProgramID      = "KNJB103C";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "edit":
                case "reset":
                    $this->callView("knjb103cForm1");
                    break 2;
                case "substaff":
                case "substaff2":
                case "substaffProctor":
                    $this->callView("knjb103cSubFormStaff"); //職員一覧画面
                    break 2;
                case "update":  //更新
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "replace":
                case "subReplace":
                    $this->callView("knjb103cSubFormReplace"); //一括更新画面
                    break 2;
                case "replace_update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->replaceModel();
                    $sessionInstance->setCmd("subReplace");
                    break 1;
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
$knjb103cCtl = new knjb103cController();
//var_dump($_REQUEST);
