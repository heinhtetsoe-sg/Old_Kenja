<?php

require_once('for_php7.php');

require_once('knjz290Model.inc');
require_once('knjz290Query.inc');

class KNJZ290Controller extends Controller {
	var $ModelClassName = "KNJZ290Model";
	var $ProgramID		= "KNJZ290";
	
	function main()
	{
		$sessionInstance =& Model::getModel($this);
		$sessionInstance->knjz290Model();		//コントロールマスタの呼び出し
		while ( true ) {
			switch (trim($sessionInstance->cmd)) {
				case "edit":
					$this->callView("edit");
					break 2;
				case "addupddel":		//保存ボタン（追加・更新・取消）
					$sessionInstance->getUpdateModel();
					//変更済みの場合は詳細画面に戻る
					//分割フレーム作成
					$this->callView("list");
					break 2;
				case "copy":		//前年度からコピー
					$sessionInstance->getCopyModel();
					//変更済みの場合は詳細画面に戻る
					//分割フレーム作成
					$this->callView("list");
					break 2;
				case "list":
				case "clear":
					$this->callView("list");
					break 2;
				case "error":
					$this->callView("error");
					break 2;
				case "":
					//分割フレーム作成
					$args["left_src"] = "knjz290index.php?cmd=list";
					$args["right_src"] = "knjz290index.php?cmd=edit";
					$args["cols"] = "75%,25%";
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
$KNJZ290Ctl = new KNJZ290Controller;
//var_dump($_REQUEST);
?>
