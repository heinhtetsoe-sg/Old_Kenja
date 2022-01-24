<?php

require_once('for_php7.php');

require_once('knjx_reason_collection_selectModel.inc');
require_once('knjx_reason_collection_selectQuery.inc');

class knjx_reason_collection_selectController extends Controller
{
    public $ModelClassName = "knjx_reason_collection_selectModel";
    public $ProgramID      = "KNJX_REASON_COLLECTION_SELECT";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $this->callView("knjx_reason_collection_selectForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjx_reason_collection_selectCtl = new knjx_reason_collection_selectController();
